package cn.keeponline.telegram.controller;

import cn.keeponline.telegram.entity.UserTask;
import cn.keeponline.telegram.mapper.UserTaskMapper;
import cn.keeponline.telegram.service.impl.TaskServiceImpl;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}
