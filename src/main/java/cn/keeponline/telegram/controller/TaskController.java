package cn.keeponline.telegram.controller;

import cn.hutool.core.util.StrUtil;
import cn.keeponline.telegram.entity.SystemConfigs;
import cn.keeponline.telegram.entity.UserInfo;
import cn.keeponline.telegram.entity.UserPackage;
import cn.keeponline.telegram.exception.BizzRuntimeException;
import cn.keeponline.telegram.input.*;
import cn.keeponline.telegram.mapper.SystemConfigsMapper;
import cn.keeponline.telegram.mapper.UserInfoMapper;
import cn.keeponline.telegram.mapper.UserPackageMapper;
import cn.keeponline.telegram.response.Response;
import cn.keeponline.telegram.schedule.RestartAbnormalTask;
import cn.keeponline.telegram.service.TaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/task")
@Api(tags = "任务相关")
@Slf4j
public class TaskController extends ControllerBase {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private SystemConfigsMapper systemConfigsMapper;

    @Autowired
    private RestartAbnormalTask restartAbnormalTask;

    @Qualifier("userPackageMapper")
    @Autowired
    private UserPackageMapper userPackageMapper;

    @Qualifier("redisTemplate1")
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @ApiOperation("批量添加任务")
    @PostMapping("/addBatchTask")
    public Response addBatchTask(@Valid AddBatchTaskInput addBatchTaskInput) throws Exception {
        log.info("addBatchTaskInput: {}", addBatchTaskInput);
        if (addBatchTaskInput.getFile() == null && StrUtil.isBlank(addBatchTaskInput.getMessageContent())) {
            throw new BizzRuntimeException("必须得有发送内容或图片");
        }
        List<String> uids = addBatchTaskInput.getUids();
        userPackageMapper.updateExpire();
        List<UserPackage> userPackages = userPackageMapper.listByUidsAndStatus(uids, 1);

        Map<String, Date> map = userPackages.stream().collect(Collectors.toMap(UserPackage::getUid, UserPackage::getExpireTime));

        if (map.size() != uids.size()) {
            for (String uid : uids) {
                Date date = map.get(uid);
                if (date == null) {
                    UserInfo userInfo = userInfoMapper.getByUid(uid);
                    String nickname = userInfo.getNickname();
                    throw new BizzRuntimeException(nickname + ",该账号绑定的套餐已失效，去掉该账号再试试");
                }
            }
        }

        Integer sendInterval = addBatchTaskInput.getSendInterval();
        SystemConfigs systemConfigs = systemConfigsMapper.getByKey("sendInterval");
        int interval = Integer.parseInt(systemConfigs.getValue());

        if (sendInterval < interval) {
            throw new BizzRuntimeException("间隔时间不能小于" + interval + "秒");
        }
        taskService.addBatch(addBatchTaskInput);
        Thread.sleep(3000);
        return Response.success();
    }

    @ApiOperation("修改频率")
    @PostMapping("/updateFrequency")
    public Response updateFrequency(@Valid UpdateFrequencyInput updateFrequencyInput) throws Exception {
        log.info("updateFrequencyInput: {}", updateFrequencyInput);
        taskService.updateFrequency(updateFrequencyInput);
        return Response.success();
    }

    @ApiOperation("停止任务")
    @GetMapping("/stopTask")
    public Response stopTask(StopTaskInput stopTaskInput) throws Exception {
        log.info("stopTaskInput: {}", stopTaskInput);
        taskService.stopTask(stopTaskInput);
        return Response.success();
    }

    @ApiOperation("停止所有任务")
    @GetMapping("/stopAllTask")
    public Response stopAllTask(StopAllTaskInput stopAllTaskInput) throws Exception {
        log.info("stopAllTaskInput: {}", stopAllTaskInput);
        taskService.stopAllTask(stopAllTaskInput);
        return Response.success();
    }

    @ApiOperation("测试重启任务")
    @GetMapping("/testRestart")
    public Response testRestart() throws Exception {
        restartAbnormalTask.restartAbnormalTask();
        return Response.success();
    }

    @ApiOperation("获取群组信息")
    @GetMapping("/getGroups")
    public Response getGroups(String token) throws Exception {
        return Response.success(taskService.getGroups(token));
    }

    @ApiOperation("添加好友")
    @GetMapping("/addFriends")
    public Response addFriends(String groupId, String remark, String uid) {
        log.info("groupId: {}, remark: {}, uid: {}", groupId, remark, uid);

        if (!Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent("addFriends:" + uid, LocalDateTime.now().toString(), Duration.ofDays(10)))) {
            throw new BizzRuntimeException("上次任务还没有执行完成");
        }
        try {
            taskService.addFriends(groupId, remark, uid);
        } catch (BizzRuntimeException be) {
            log.error("业务异常", be);
            throw be;
        } catch (Exception e) {
            log.error("系统异常", e);
        } finally {
            redisTemplate.delete("addFriends:" + uid);
        }
        return Response.success();
    }
}
