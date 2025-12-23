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
import okhttp3.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.keeponline.telegram.service.impl.TaskServiceImpl.smetaMap;

@RestController
@Slf4j
public class TestController {
    @Autowired
    private UserTaskMapper userTaskMapper;

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

    @RequestMapping("/getWS")
    public void getWS() {
        Map<String, WebSocket> webSocketMap = TaskServiceImpl.WebSocketMap;
        log.info("socket数量: {}", webSocketMap.size());
        log.info("socket信息: {}", webSocketMap);
    }

    @RequestMapping("/getSmata")
    public void getSmata() {
        log.info("smata数量: {}", smetaMap.size());
        log.info("smeta信息: {}", smetaMap);
    }

    @RequestMapping("/sendImageToGroupUser")
    public String sendImageToGroupUser(@RequestParam("token") String token,
                                       @RequestParam("uid") String uid, @RequestParam("groupId") String groupId) {
        Map<String, WebSocketWrapper> uuuSocketMap = TaskServiceImpl.uuuSocketMap;
        WebSocketWrapper webSocketWrapper = uuuSocketMap.get(token);
        if (webSocketWrapper == null) {
            return "ws 未连接";
        }

        UuutalkApiClient uuutalkApiClient = new UuutalkApiClient();
        List<UUUGroupMemberDTO> uuuGroupMemberDTOS = new ArrayList<>();
        try {
            uuuGroupMemberDTOS = uuutalkApiClient.syncGroupMembers(token, groupId, 0, 100);
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
                log.info("发送图片: {}", JSONUtil.toJsonStr(uuuGroupMemberDTO));
                UUTalkWsCore.sendPictureMessage(webSocketWrapper,
                        "file/preview/chat/1/06e1321af60a42bf8ec6b15facfaaf3d/1a3ba617f3384eb8374255a27ecdb7b7.jpg",
                        to_uid,
                        1,
                        uid);
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return "发送成功";
    }


}
