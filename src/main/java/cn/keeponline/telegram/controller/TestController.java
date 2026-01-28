package cn.keeponline.telegram.controller;

import cn.hutool.json.JSONUtil;
import cn.keeponline.telegram.dto.uuudto.UUUGroupMemberDTO;
import cn.keeponline.telegram.entity.UserTask;
import cn.keeponline.telegram.mapper.UserTaskMapper;
import cn.keeponline.telegram.service.impl.TaskServiceImpl;
import cn.keeponline.telegram.talktools.services.UuutalkApiClient;
import cn.keeponline.telegram.talktools.ws.UUTalkWsCore;
import cn.keeponline.telegram.talktools.ws.WebSocketWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@RestController
@Slf4j
public class TestController {
    @Autowired
    private UserTaskMapper userTaskMapper;

    @Autowired
    private UuutalkApiClient uuutalkApiClient;

    @RequestMapping("/test")
    public String test(String messageContent) {
        for (int i = 0; i < 1000; i++) {
            UserTask userTask = new UserTask();
            userTask.setUid(i + "");
            userTask.setAccountId(i + "");
            userTask.setStatus(1);
            userTask.setSendInterval(5000);
            userTaskMapper.insert(userTask);
        }
        return "success";
    }

    @RequestMapping("/v1/dfapi/add")
    public String test2() {
        return """
                {"status":"success", "transaction_id":"12345"}
                """;
    }

    @RequestMapping("/v1/dfapi/query_order")
    public String query_order() {
        return """
                {"status":"success", "refCode":"3"}
                """;
    }

    @RequestMapping("/getWSAndClose")
    public String getWS() {
        Map<String, WebSocketWrapper> uuuSocketMap = TaskServiceImpl.uuuSocketMap;
        log.info("ws信息: {}", uuuSocketMap);
        for (WebSocketWrapper ws : uuuSocketMap.values()) {
            ws.close();
        }
        return "请查看日志";
    }


    @RequestMapping("/sendImageToGroupUser")
    public String sendImageToGroupUser(@RequestParam(value = "token", defaultValue = "04a24819bce74ad894a416c0177bd67e") String token,
                                       @RequestParam(value = "uid", defaultValue = "59ae5a30bcf34c349b135b9c8fccac86") String uid,
                                       @RequestParam(value = "groupId", defaultValue = "e5c166e8f6044bc78f483bf0fdb4834a") String groupId) throws InterruptedException {
        Map<String, WebSocketWrapper> uuuSocketMap = TaskServiceImpl.uuuSocketMap;
        WebSocketWrapper webSocketWrapper = uuuSocketMap.get(uid);
        if (webSocketWrapper == null) {
            return "ws 未连接";
        }

        List<UUUGroupMemberDTO> uuuGroupMemberDTOS;
        try {
            uuuGroupMemberDTOS = uuutalkApiClient.syncGroupMembers(groupId, token, 0, 100);
        } catch (IOException e) {
            log.error("", e);
            return "拉取群成员失败";
        }
        if (CollectionUtils.isEmpty(uuuGroupMemberDTOS)) {
            return "拉取群成员为空";
        }

        for (UUUGroupMemberDTO uuuGroupMemberDTO : uuuGroupMemberDTOS) {
            try {
                String to_uid = uuuGroupMemberDTO.getUid();
                Integer role = uuuGroupMemberDTO.getRole();
                if (to_uid.equals(uid) || role == 1) {
                    continue;
                }

                Map<String, String> stranger = uuutalkApiClient.createStranger(to_uid, token);
                log.info("strange: {}", stranger);
                log.info("发送图片: {}", JSONUtil.toJsonStr(uuuGroupMemberDTO));
                UUTalkWsCore.sendPictureMessage(webSocketWrapper,
                        "file/preview/chat/1/06e1321af60a42bf8ec6b15facfaaf3d/1a3ba617f3384eb8374255a27ecdb7b7.jpg",
                        to_uid,
                        1,
                        uid);
            } catch (Exception e) {
                log.error("", e);
            }
            Thread.sleep(50000L);
        }
        return "发送成功";
    }

    public static void main(String[] args) throws IOException {
        // 06e1321af60a42bf8ec6b15facfaaf3d

        String groupId = "06e1321af60a42bf8ec6b15facfaaf3d";
        String token = "04a24819bce74ad894a416c0177bd67e";
//        List<UUUGroupMemberDTO> uuuGroupMemberDTOS = uuutalkApiClient.syncGroupMembers(groupId, token, 0, 100);
//        log.info(JSONUtil.toJsonStr(uuuGroupMemberDTOS));

    }
}
