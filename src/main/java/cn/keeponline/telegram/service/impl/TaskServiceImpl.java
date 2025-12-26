package cn.keeponline.telegram.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.keeponline.telegram.component.AsyncComponent;
import cn.keeponline.telegram.context.SysUserContext;
import cn.keeponline.telegram.dto.*;
import cn.keeponline.telegram.dto.uuudto.UUUFriendDTO;
import cn.keeponline.telegram.dto.uuudto.UUUGroupDTO;
import cn.keeponline.telegram.dto.ws.GmsgDTO;
import cn.keeponline.telegram.dto.ws.MarksDTO;
import cn.keeponline.telegram.dto.ws.SmetaDTO;
import cn.keeponline.telegram.entity.MsgRecord;
import cn.keeponline.telegram.entity.SendRecord;
import cn.keeponline.telegram.entity.UserInfo;
import cn.keeponline.telegram.entity.UserTask;
import cn.keeponline.telegram.exception.BizzRuntimeException;
import cn.keeponline.telegram.input.*;
import cn.keeponline.telegram.mapper.MsgRecordMapper;
import cn.keeponline.telegram.mapper.UserInfoMapper;
import cn.keeponline.telegram.mapper.UserTaskMapper;
import cn.keeponline.telegram.service.TaskService;
import cn.keeponline.telegram.talktools.services.OssMultipartUploader;
import cn.keeponline.telegram.talktools.services.UuutalkApiClient;
import cn.keeponline.telegram.talktools.uutalk.UUTalkClient;
import cn.keeponline.telegram.talktools.ws.UUTalkWsCore;
import cn.keeponline.telegram.talktools.ws.WebSocketWrapper;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TaskServiceImpl implements TaskService {
    // 保留所有的连接信息
    public static Map<String, WebSocket> WebSocketMap = new ConcurrentHashMap<>();

    // 保留所有的连接信息
    public static Map<String, WebSocketWrapper> uuuSocketMap = new ConcurrentHashMap<>();

    // 保留用户的群组元数据，可以去更新
    public static Map<String, Map<String, Long>> smetaMap = new ConcurrentHashMap<>();

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    // uid -> scheduled task
    private final Map<String, ScheduledFuture<?>> scheduledTaskMap = new ConcurrentHashMap<>();

    // uid -> current send index
    private final Map<String, Integer> sendIndexMap = new ConcurrentHashMap<>();

    public static final Map<String, Integer> statusMap = new ConcurrentHashMap<>();

    @Autowired
    private UserTaskMapper userTaskMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private SysUserContext sysUserContext;

    @Autowired
    @Lazy
    private TaskService taskService;

    @Autowired
    private MsgRecordMapper msgRecordMapper;

    @Autowired
    private AsyncComponent asyncComponent;

    @Autowired
    private UUTalkClient uuTalkClient;

    @Override
    @Async("asyncTaskExecutor")
    public void addBatch(AddBatchTaskInput addBatchTaskInput) throws Exception {
        Integer sendInterval = addBatchTaskInput.getSendInterval() * 1000;
        String messageContent = addBatchTaskInput.getMessageContent();
        Integer cvsType = addBatchTaskInput.getCvsType();
        List<String> uids = addBatchTaskInput.getUids();
        if (uids.isEmpty()) {
            return;
        }
        List<UserInfo> userInfos = userInfoMapper.listByUids(uids);
        if (userInfos == null || userInfos.isEmpty()) {
            return;
        }
        // 删除这些uid的所有群组任务
        int delCount = userTaskMapper.deleteByUidsAndCvsType(uids, null);
        log.info("删除任务数量: {}", delCount);

        UserInfo userInfo = userInfos.get(0);
        String uid = userInfo.getUid();
        String token = userInfo.getToken();

        UserTask userTask = new UserTask();
        userTask.setAccountId(userInfo.getAccountId());
        userTask.setStatus(1);
        userTask.setSendInterval(sendInterval);
        userTask.setMessageContent(messageContent);
        userTask.setCvsType(cvsType);
        userTask.setType(1);
        MultipartFile file = addBatchTaskInput.getFile();
        if (file != null) {
            String md5 = DigestUtils.md5DigestAsHex(file.getInputStream());
            long size = file.getSize();
            String path = System.getProperty("user.home") + "/yunipicture";
            File uploadDir = new File(path);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            File tempFile = new File(uploadDir, md5+".png");
            Files.copy(file.getInputStream(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            String objectKey = "chat/2/d88d5141821740aeaa6366776f95dd50/{md5}.png".replace("{md5}", md5);
            OssMultipartUploader.multipartUploadOnePart(token, tempFile.getAbsolutePath(), objectKey);

            userTask.setMd5(md5);
            userTask.setFileName("file/preview/" + objectKey);
            userTask.setFileSize(size);
            userTask.setType(2);
        }
        for (UserInfo user : userInfos) {
            UserTask userTaskDB = new UserTask();
            BeanUtils.copyProperties(userTask, userTaskDB);
            userTaskDB.setUid(user.getUid());
            userTaskMapper.insert(userTaskDB);
            statusMap.put(uid, 1);
            taskService.asyncRestartTask(userTaskDB);
        }
    }

    @Override
    @Async("asyncTaskExecutor")
    public void asyncRestartTask(UserTask userTask) throws Exception {
        String uid = userTask.getUid();
        Integer cvsType = userTask.getCvsType();
        UserInfo userInfo = userInfoMapper.getByUid(uid);
        if (userInfo == null) {
            log.info("[asyncRestartTask]找不到用户, uid: {}", uid);
            return;
        }
        if (userInfo.getStatus() == 0) {
            log.info("[asyncRestartTask]用户状态失效, uid: {}", uid);
            return;
        }
        String token = userInfo.getToken();
//        JSONObject wssInfo = SendMessage.getAccessToken(uid, token);
//        log.info("wssInfo: {}", JSON.toJSONString(wssInfo));
//        Integer ec = wssInfo.getInteger("ec");
//        if (ec != 200) {
//            // 用户状态改一下，任务状态不用动
//            userInfo.setStatus(0);
//            userInfoMapper.updateById(userInfo);
//            log.info("[asyncRestartTask]修改用户状态成功: {}", JSON.toJSONString(userInfo));
//            return;
//        }
//        String accessToken = wssInfo.getJSONObject("data").getString("access_token");
        WebSocketWrapper ws = uuTalkClient.runWsClient(uid, token);
        uuuSocketMap.put(uid, ws);
        statusMap.put(uid, 1);
        // 等待一下，让ws把状态修改过来
        Thread.sleep(1500);
        List<SendGeneralDTO> list = new ArrayList<>();
        UuutalkApiClient uuutalkApiClient = new UuutalkApiClient();
        if (cvsType == 2) {
            List<UUUGroupDTO> groupsList = uuutalkApiClient.getGroups(token);
            for (UUUGroupDTO uuuGroupDTO : groupsList) {
                Integer forbidden = uuuGroupDTO.getForbidden();
                Integer role = uuuGroupDTO.getRole();
                if (forbidden == 1 && role == 0) { // 这个是禁言并且自己不是管理员，这些群就不要发消息了
                    continue;
                }
                SendGeneralDTO sendGeneralDTO = new SendGeneralDTO();
                sendGeneralDTO.setId(uuuGroupDTO.getGroup_no());
                sendGeneralDTO.setName(uuuGroupDTO.getName());
                sendGeneralDTO.setForbidden(forbidden);
                sendGeneralDTO.setChannelType(2);
                list.add(sendGeneralDTO);
            }
        } else {
            List<UUUFriendDTO> friends = uuutalkApiClient.getFriends(token);
            for (UUUFriendDTO friend : friends) {
                if (friend.getUid().equals("u_10000") || friend.getUid().equals("fileHelper")) {
                    continue;
                }
                SendGeneralDTO sendGeneralDTO = new SendGeneralDTO();
                sendGeneralDTO.setId(friend.getUid());
                sendGeneralDTO.setName(friend.getName());
                sendGeneralDTO.setChannelType(1);
                list.add(sendGeneralDTO);
            }
        }
        try {
            startScheduleSend(userTask, list, ws);
        } catch (Exception e) {
            WebSocketMap.remove(uid);
            // 修改任务状态、break循环
            userTask.setStatus(0);
            if (userTaskMapper.updateById(userTask) == 1) {
                log.error("发生异常，修改任务状态成功: {}", JSON.toJSONString(userTask), e);
            }
        }
    }

    private void startScheduleSend(UserTask userTask,
                                   List<SendGeneralDTO> list,
                                   WebSocketWrapper ws) {
        String uid = userTask.getUid();
        // stop existing task if exists
        stopSchedule(uid);

        sendIndexMap.put(uid, 0);

        Integer delayMs = userTask.getSendInterval();

        ScheduledFuture<?> future =
                taskScheduler.scheduleWithFixedDelay(() -> {
                    try {
                        sendOnce(userTask, list, ws);
                    } catch (Exception e) {
                        log.error("scheduled send error, uid={}", uid, e);
                        stopSchedule(uid);
                    }
                }, Duration.ofMillis(delayMs));

        scheduledTaskMap.put(uid, future);
    }

    private void sendOnce(UserTask userTask,
                          List<SendGeneralDTO> list,
                          WebSocketWrapper ws) throws Exception {

        String uid = userTask.getUid();
        String content = userTask.getMessageContent();
        String fileName = userTask.getFileName();
        Integer status = statusMap.get(uid);
        if (status == null || status == 0) {
            log.info("任务状态异常，停止执行，uid: {}", uid);
            stopSchedule(uid);
            return;
        }

        int index = sendIndexMap.getOrDefault(uid, 0);
        if (index >= list.size()) {
            index = 0;
        }

        SendGeneralDTO dto = list.get(index);
        sendIndexMap.put(uid, index + 1);

        String gid = dto.getId();
        String name = dto.getName();
        Integer channelType = dto.getChannelType();


        boolean send = true;
        if (StrUtil.isBlank(fileName)) {
            send = UUTalkWsCore.sendTextMessage(ws, content, gid, channelType, uid);
        } else {
            send = UUTalkWsCore.sendPictureMessage(ws, fileName, gid, channelType, uid);
        }

        SendRecord sendRecord = new SendRecord();
        sendRecord.setUid(uid);
        sendRecord.setGroupName(name);

        if (!send) {
            sendRecord.setStatus(0);
            sendRecord.setReason("发送失败");
            asyncComponent.insert(sendRecord);
            userTask.setStatus(0);
            userTaskMapper.updateById(userTask);
            stopSchedule(uid);
            statusMap.put(uid, 0);
        } else {
            sendRecord.setStatus(1);
            asyncComponent.insert(sendRecord);
        }
    }

    private void stopSchedule(String uid) {
        ScheduledFuture<?> f = scheduledTaskMap.remove(uid);
        if (f != null) {
            f.cancel(false);
        }
        sendIndexMap.remove(uid);
    }

    @Override
    public void stopTask(StopTaskInput stopTaskInput) {
        Integer cvsType = stopTaskInput.getCvsType();
        String uid = stopTaskInput.getUid();
        String accountId = sysUserContext.getAccountId();
        UserTask userTask = userTaskMapper.getByAccountIdAndUidAndCvsType(accountId, uid, cvsType);
        if (userTask == null) {
            throw new BizzRuntimeException("未找到对应的任务");
        }
        userTaskMapper.deleteById(userTask.getId());
        statusMap.remove(uid);
        WebSocketWrapper ws = uuuSocketMap.remove(uid);
        if (ws != null) {
            ws.close();
        }
    }

    @Override
    public void stopAllTask(StopAllTaskInput stopAllTaskInput) {
        String accountId = sysUserContext.getAccountId();
        log.info("执行删除所有任务: {}", accountId);
        List<String> uids = stopAllTaskInput.getUids();
        for (String uid : uids) {
            List<UserTask> userTasks = userTaskMapper.listByUid(uid);
            for (UserTask userTask : userTasks) {
                userTaskMapper.deleteById(userTask.getId());
                log.info("删除成功: {}", uid);
                statusMap.remove(uid);
                WebSocketWrapper ws = uuuSocketMap.remove(uid);
                if (ws != null) {
                    ws.close();
                }
            }
        }

    }

    @Override
    public void updateFrequency(UpdateFrequencyInput updateFrequencyInput) throws Exception {
        List<String> uids = updateFrequencyInput.getUids();
        if (uids == null || uids.isEmpty()) {
            return;
        }
        Integer sendInterval = updateFrequencyInput.getSendInterval() * 1000;

        int updateCount = userTaskMapper.updateFrequency(sendInterval, uids, 2);
        log.info("修改频率数量: {}", updateCount);
    }

    public WebSocket getWebSocket(String uid, String accessToken) {
        WebSocket webSocket = WebSocketMap.get(uid);
        if (webSocket != null) {
            return webSocket;
        }
        Request request = new Request.Builder()
                .url("wss://sn.im.uneedx.com/v2/gas/web")
//                .url("ws://localhost:8080/websocket")
                .addHeader("Origin", "https://wstool.jackxiang.com")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.9")
                .addHeader("Pragma", "no-cache")
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36")
                .addHeader("Sec-WebSocket-Version", "13")
                .build();
        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, okhttp3.Response response) {
                log.info("【yuni】开启连接, uid: {}", uid);
                UserTask userTask = userTaskMapper.getByUidAndCvsTypeAndStatus(uid, null, 0);
                if (userTask != null) {
                    userTask.setStatus(1);
                    if (userTaskMapper.updateById(userTask) == 1) {
                        log.info("【yuni】连接ws成功，修改任务状态为正常");
                    }
                }
                String authMessage = """
                {"header":{"sm":1,"ver":10,"uid":"{uid}","cmdtype":"g.auth"},"body":{"uid":"{uid}","gameid":"nimo-web","access_token":"{access_token}","ua":"UneedGroup/3.8.0 BROWSER/8.5.5 (win32-x64;Release;Build-0-9-0)","timezone":28800,"deviceid":"Browser"}}
                """.stripTrailing()
                        .replace("{uid}", uid)
                        .replace("{access_token}", accessToken);
//                log.info("authMessage: {}", authMessage);
                ws.send(authMessage);
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
//                log.info("【yuni】📩 收到消息: {}", text);
                WSResponse wsResponse = JSON.parseObject(text, WSResponse.class);
                HeaderDTO header = wsResponse.getHeader();
                String cmdtype = header.getCmdtype();
                switch (cmdtype) {
                    case "g.auth":
                        String smetaSendMessage = """
                {"header":{"sm":1,"ver":10,"uid":"{uid}","cmdtype":"g.smeta"},"body":{"marks":null}}
                """.stripTrailing()
                                .replace("{uid}", uid);
                        ws.send(smetaSendMessage);

                        String syncmeta = """
                {"header":{"sm":1,"ver":10,"uid":"{uid}","cmdtype":"g.syncmeta"},"body":{}}
                """.stripTrailing()
                                .replace("{uid}", uid);
                        ws.send(syncmeta);

                        String ping = """
                {"header":{"sm":1,"ver":10,"uid":"{uid}","cmdtype":"g.ping"},"body":{"uid":"{uid}","timestamp":{timestamp}}}
                """.stripTrailing()
                                .replace("{uid}", uid)
                                .replace("{timestamp}", new Date().getTime() / 1000 + "");
                        ws.send(ping);
                        break;
                    case "g.smeta":
//                        log.info("【yuni】smeta消息");
                        WSResponse<SmetaDTO> smeta = JSON.parseObject(text, new TypeReference<>() {});
                        List<MarksDTO> marks = smeta.getBody().getMarks();
                        Map<String, Long> collect = marks.stream().filter(x -> x.getType() == 2).collect(Collectors.toMap(MarksDTO::getId, MarksDTO::getOffset));
                        smetaMap.put(uid, collect);
                        break;
                    case "g.psh":
//                        log.info("【yuni】psh消息");
                        smeta = JSON.parseObject(text, new TypeReference<>() {});
                        marks = smeta.getBody().getMarks();
                        for (MarksDTO mark : marks) {
                            String id = mark.getId();// 这个地方就算拿到了群组id
                            Integer type = mark.getType();
                            if (type != 2) {
                                continue;
                            }
                            Map<String, Long> stringLongMap = smetaMap.get(uid);
                            Long offset = 0L;
                            if (stringLongMap != null) {
//                                log.info("使用已经更新过的offset");
                                offset = stringLongMap.get(id);
                            }
                            // 这个地方要去主动拉消息
                            String syncMsg = """
                                    {"header":{"sm":1,"ver":10,"uid":"{uid}","cmdtype":"g.sync"},"body":{"marks":[{"id":"{id}","type":{type},"offset":{offset}}]}}
                                    """
                                    .replace("{uid}", uid)
                                    .replace("{id}", id)
                                    .replace("{type}", type + "")
                                    .replace("{offset}", offset + "");
                            ws.send(syncMsg);
//                            log.info("【yuni】sync消息发送成功, offset: {}", offset);
                        }
                        break;
                    case "g.gmsg":
                        // 这个还是得再回复，不然会一直给你推送这些消息
                        WSResponse<GmsgDTO> gmsg = JSON.parseObject(text, new TypeReference<>() {});
                        GmsgDTO body = gmsg.getBody();
                        MsgHeadDTO msgHead = body.getMsg_head();
                        MsgBodyDTO msgBody = body.getMsg_body();
                        Long offset = msgHead.getOffset();// 这个offset如果大于map中的offset，就更新下
                        Integer cvsType = msgHead.getCvs_type();
                        if (cvsType != 2) {
                            break;
                        }
                        String remoteid = header.getRemoteid();
                        Integer msgType = msgHead.getMsg_type();
                        String sfin = """
                                {"header":{"sm":1,"ver":10,"uid":"{uid}","cmdtype":"g.sfin"},"body":{"marks":[{"id":"{remoteid}","type":2,"offset":{offset}}]}}
                                """
                                .stripTrailing()
                                .replace("{uid}", uid)
                                .replace("{remoteid}", remoteid)
                                .replace("{offset}", offset + "");
                        String msgContent = "";
                        if (msgBody.getImages() != null) {
                            List<PictureDTO> images = msgBody.getImages();
//                            log.info("gid: {}, pictureDTO: {}", remoteid, JSON.toJSONString(images));
                            msgContent = images.get(0).getThumb_url();
                            // 这种类型的图片访问不了
                            if (msgContent.contains("uneed-file-private")) {
                                ws.send(sfin);
                                break;
                            }
                        } else {
                            msgContent = msgBody.getText();
                        }
                        MsgRecord msgRecord = new MsgRecord();
                        msgRecord.setMsgContent(msgContent);
                        msgRecord.setMsgType(msgType);
                        msgRecord.setDate(new Date());
                        msgRecord.setUid(uid);
                        msgRecord.setGid(remoteid);
                        if (StrUtil.isNotBlank(msgContent)) {
                            msgRecordMapper.insertIgnore(msgRecord);
                        }
                        Map<String, Long> map = new HashMap<>();
                        map.put(remoteid, offset);
                        smetaMap.put(uid, map);
//                        log.info("更新smetaMap, uid: {}, remoteid: {}, offset: {}", uid, remoteid, offset);
                        // TODO KES 这个推送里面的type是不是就是cvs_type，应该是的
                        ws.send(sfin);
                        break;
                }
            }

            @Override
            public void onMessage(WebSocket ws, ByteString bytes) {
//                System.out.println("【yuni】📦 收到二进制消息: " + bytes.hex());
            }

            @Override
            public void onClosing(WebSocket ws, int code, String reason) {
                log.error("🚪 连接关闭, uid: {}, reason: {}", uid, reason);
                ws.close(1000, null);
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, okhttp3.Response response) {
                ws.close(1001, "client closing due to failure");
                WebSocketMap.remove(uid);
                List<UserTask> userTasks = userTaskMapper.listByUidAndStatus(uid, 1);
                for (UserTask userTask : userTasks) {
                    userTask.setStatus(0);
                    if (userTaskMapper.updateById(userTask) == 1) {
                        log.info("断开连接，修改任务状态成功: {}", JSON.toJSONString(userTask));
                    }
                    statusMap.put(uid, 0);
                }
                log.error("❌ 连接失败: {}", t.getMessage());
            }
        };
        OkHttpClient client = new OkHttpClient();

        // 建立连接
        return client.newWebSocket(request, listener);
    }

    public static void main(String[] args) {
        String smeta = """
{"header":{"sm":1,"ver":10,"uid":"{uid}","cmdtype":"g.smeta"},"body":{"marks":null}}
    """.stripTrailing();
        System.out.println(smeta);
        System.out.println("111");
        System.out.println();
    }
}
