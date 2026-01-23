package cn.keeponline.telegram.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.keeponline.telegram.component.AsyncComponent;
import cn.keeponline.telegram.context.SysUserContext;
import cn.keeponline.telegram.dto.*;
import cn.keeponline.telegram.dto.uuudto.UUUFriendDTO;
import cn.keeponline.telegram.dto.uuudto.UUUGroupDTO;
import cn.keeponline.telegram.entity.SendRecord;
import cn.keeponline.telegram.entity.UserInfo;
import cn.keeponline.telegram.entity.UserTask;
import cn.keeponline.telegram.exception.BizzRuntimeException;
import cn.keeponline.telegram.input.*;
import cn.keeponline.telegram.mapper.UserInfoMapper;
import cn.keeponline.telegram.mapper.UserTaskMapper;
import cn.keeponline.telegram.service.TaskService;
import cn.keeponline.telegram.talktools.services.OssMultipartUploader;
import cn.keeponline.telegram.talktools.services.UuutalkApiClient;
import cn.keeponline.telegram.talktools.uutalk.UUTalkClient;
import cn.keeponline.telegram.talktools.ws.UUTalkWsCore;
import cn.keeponline.telegram.talktools.ws.WebSocketWrapper;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
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

@Service
@Slf4j
public class TaskServiceImpl implements TaskService {

    // 保留所有的连接信息
    public static Map<String, WebSocketWrapper> uuuSocketMap = new ConcurrentHashMap<>();

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
    private AsyncComponent asyncComponent;

    @Autowired
    private UUTalkClient uuTalkClient;

    @Qualifier("redisTemplate1")
    @Autowired
    private RedisTemplate<String, Object> redisTemplate1;

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
        WebSocketWrapper ws = uuTalkClient.runWsClient(uid, token);
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
            uuuSocketMap.remove(uid);
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

//        int index = sendIndexMap.getOrDefault(uid, 0);
        Object indexInRedis = redisTemplate1.opsForValue().get("index:" + uid);
        if (indexInRedis == null) {
            indexInRedis = "0";
        }
        int index = Integer.parseInt(indexInRedis.toString());
        if (index >= list.size()) {
            index = 0;
            if (userTask.getCvsType() == 1) {
                statusMap.remove(uid);
                userTaskMapper.deleteById(userTask.getId());
                redisTemplate1.opsForValue().set("index:" + uid, "0");
                return;
            }
        }

        SendGeneralDTO dto = list.get(index);
        // 存redis里面去吧，从redis中拿
//        sendIndexMap.put(uid, index + 1);
        redisTemplate1.opsForValue().set("index:" + uid, String.valueOf(index + 1));

        String gid = dto.getId();
        String name = dto.getName();
        Integer channelType = dto.getChannelType();


        boolean send = true;
        if (StrUtil.isBlank(fileName)) {
//            send = UUTalkWsCore.sendTextMessage(ws, content, gid, channelType, uid);
        } else {
//            send = UUTalkWsCore.sendPictureMessage(ws, fileName, gid, channelType, uid);
        }

        SendRecord sendRecord = new SendRecord();
        sendRecord.setUid(uid);
        sendRecord.setGroupName(name);
        sendRecord.setId(UUID.randomUUID().toString());
        sendRecord.setCreateTime(new Date());
        sendRecord.setModifyTime(new Date());

        if (!send) {
            sendRecord.setStatus(0);
            sendRecord.setReason("发送失败");
            asyncComponent.insertSendRecord(sendRecord);
            userTask.setStatus(0);
            userTaskMapper.updateById(userTask);
            stopSchedule(uid);
            statusMap.put(uid, 0);
        } else {
            sendRecord.setStatus(1);
            asyncComponent.insertSendRecord(sendRecord);
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



    public static void main(String[] args) {
        String smeta = """
{"header":{"sm":1,"ver":10,"uid":"{uid}","cmdtype":"g.smeta"},"body":{"marks":null}}
    """.stripTrailing();
        System.out.println(smeta);
        System.out.println("111");
        System.out.println();
    }
}
