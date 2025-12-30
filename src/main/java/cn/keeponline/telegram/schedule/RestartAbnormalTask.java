package cn.keeponline.telegram.schedule;

import cn.hutool.core.util.StrUtil;
import cn.keeponline.telegram.dto.uuudto.UUURegionDTO;
import cn.keeponline.telegram.entity.UserPackage;
import cn.keeponline.telegram.entity.UserTask;
import cn.keeponline.telegram.mapper.SendRecordMapper;
import cn.keeponline.telegram.mapper.SystemConfigsMapper;
import cn.keeponline.telegram.mapper.UserPackageMapper;
import cn.keeponline.telegram.mapper.UserTaskMapper;
import cn.keeponline.telegram.service.TaskService;
import cn.keeponline.telegram.talktools.services.UuutalkApiClient;
import cn.keeponline.telegram.talktools.ws.UUTalkWsCore;
import cn.keeponline.telegram.talktools.ws.WebSocketWrapper;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static cn.keeponline.telegram.service.impl.TaskServiceImpl.uuuSocketMap;

@Component
@Slf4j
public class RestartAbnormalTask {

    @Autowired
    private UserTaskMapper userTaskMapper;

    @Autowired
    private TaskService taskService;

    @Autowired
    private SendRecordMapper sendRecordMapper;

    @Autowired
    private UserPackageMapper userPackageMapper;

    @Qualifier("systemConfigsMapper")
    @Autowired
    private SystemConfigsMapper systemConfigsMapper;

    @Scheduled(cron = "0 0/3 * * * ?")
    public void restartAbnormalTask() throws Exception {
        log.info("重启异常的任务开始执行");
        List<UserTask> userTasks = userTaskMapper.getByStatus(0);
        log.info("需要重启的任务数量: {}", userTasks.size());
        for (UserTask userTask : userTasks) {
            try {
                taskService.asyncRestartTask(userTask);
            } catch (Exception e) {
                log.error("重启任务失败", e);
            }
            Thread.sleep(2000);
        }
    }

    @Scheduled(cron = "0 0/2 * * * ?")
    public void sendPing() throws InterruptedException {
        log.info("sendPing任务开始执行");
        Map<String, WebSocketWrapper> webSocketMap = uuuSocketMap;
        log.info("socket数量: {}", webSocketMap.size());
        for (String uid : webSocketMap.keySet()) {
            WebSocketWrapper ws = webSocketMap.get(uid);
            UUTalkWsCore.sendPing(ws);
            Thread.sleep(50L);
        }
    }

    @Scheduled(cron = "0 0/15 * * * ?")
    public void updateApiAddr() throws IOException {
        log.info("updateApiAddr任务开始执行");
        UuutalkApiClient uuutalkApiClient = new UuutalkApiClient();

        Map<String, String> ping = uuutalkApiClient.ping();
        if (ping != null) {
            log.info("地址可以访问");
            return;
        }
        List<UUURegionDTO> regions = uuutalkApiClient.getRegions();
        log.info("regions: {}", JSON.toJSONString(regions));
        if (regions == null) {
            List<UUURegionDTO> regions2 = uuutalkApiClient.getRegions2();
            if (regions2 == null) {
                return;
            }
            update(regions2);
        } else {
            update(regions);
        }
    }

    private void update(List<UUURegionDTO> regions) throws IOException {
        for (UUURegionDTO region : regions) {
            String addr = region.getAddr().replace(":443", "") + "/v1";
            UuutalkApiClient.BASE_URL = addr;

            UuutalkApiClient uuutalkApiClient = new UuutalkApiClient();
            Map<String, String> ping = uuutalkApiClient.ping();
            if (ping != null) {
                log.info("地址可以访问，更新跳出: {}", addr);
                systemConfigsMapper.updateByKey("api_address", addr);
                break;
            } else {
                log.info("地址无法访问: {}", addr);
            }
        }


    }

    @Scheduled(cron = "0 0 * * * ?")
    public void deleteSendRecord()  {
        log.info("deleteSendRecord任务开始执行");
        String createTime = LocalDateTime.now().minusHours(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        int count = sendRecordMapper.deleteSendRecord(createTime);
        log.info("删除发送记录数量: {}", count);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateExpire()  {
        log.info("updateExpire任务开始执行");
        List<UserPackage> userPackages = userPackageMapper.listExpirePackage();
        for (UserPackage userPackage : userPackages) {
            String uid = userPackage.getUid();
            if (StrUtil.isBlank(uid)) {
                continue;
            }
            List<UserTask> userTasks = userTaskMapper.listByUid(uid);
            for (UserTask userTask : userTasks) {
                if (userTaskMapper.deleteById(userTask.getId()) == 1) {
                    log.info("套餐到期删除用户任务成功: {}", userTask);
                }
            }
            userPackage.setStatus(2);
            if (userPackageMapper.updateById(userPackage) == 1) {
                log.info("套餐过期修改套餐状态成功: {}", userPackage);
            }
        }

        log.info("处理过期套餐数量: {}", userPackages.size());
    }
}
