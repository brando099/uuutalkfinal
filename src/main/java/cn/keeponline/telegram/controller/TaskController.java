package cn.keeponline.telegram.controller;

import cn.hutool.core.util.StrUtil;
import cn.keeponline.telegram.entity.SystemConfigs;
import cn.keeponline.telegram.entity.UserInfo;
import cn.keeponline.telegram.entity.UserPackage;
import cn.keeponline.telegram.entity.UserTask;
import cn.keeponline.telegram.exception.BizzRuntimeException;
import cn.keeponline.telegram.input.*;
import cn.keeponline.telegram.mapper.SystemConfigsMapper;
import cn.keeponline.telegram.mapper.UserInfoMapper;
import cn.keeponline.telegram.mapper.UserPackageMapper;
import cn.keeponline.telegram.mapper.UserTaskMapper;
import cn.keeponline.telegram.response.Response;
import cn.keeponline.telegram.schedule.RestartAbnormalTask;
import cn.keeponline.telegram.service.TaskService;
import cn.keeponline.telegram.test.SendMessage;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.keeponline.telegram.service.impl.TaskServiceImpl.statusMap;
@RestController
@RequestMapping("/task")
@Api(tags = "任务相关")
@Slf4j
public class TaskController extends ControllerBase {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserTaskMapper userTaskMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private SystemConfigsMapper systemConfigsMapper;

    @Autowired
    private RestartAbnormalTask restartAbnormalTask;

    @Qualifier("userPackageMapper")
    @Autowired
    private UserPackageMapper userPackageMapper;

    @ApiOperation("添加任务")
    @PostMapping("/addTask")
    public Response addTask(@Valid AddTaskInput addTaskInput) throws Exception {
        log.info("addTaskInput: {}", addTaskInput);
        String uid = addTaskInput.getUid();
        Integer cvsType = addTaskInput.getCvsType();
        Integer sendInterval = addTaskInput.getSendInterval();
        String messageContent = addTaskInput.getMessageContent();
        if (addTaskInput.getFile() == null && StrUtil.isBlank(addTaskInput.getMessageContent())) {
            throw new BizzRuntimeException("必须得有发送内容或图片");
        }
        SystemConfigs systemConfigs = systemConfigsMapper.getByKey("sendInterval");
        int interval = Integer.parseInt(systemConfigs.getValue());

        if (sendInterval < interval) {
            throw new BizzRuntimeException("间隔时间不能小于" + interval + "秒");
        }
        if (cvsType != 1 && cvsType != 2) {
            throw new BizzRuntimeException("发送类型只能是1或2");
        }
        UserTask userTask = userTaskMapper.getByUidAndCvsTypeAndStatus(uid, cvsType, null);
        if (userTask != null) {
            throw new BizzRuntimeException("该账号存在已经在运行的任务，请停止后再运行新的任务");
        }
        UserInfo userInfo = userInfoMapper.getByUid(uid);
        if (userInfo == null) {
            throw new BizzRuntimeException("找不到用户");
        }
        UserPackage userPackage = userPackageMapper.getByUidAndStatus(uid, 1);
        if (userPackage == null) {
            throw new BizzRuntimeException("账号关联的套餐无效");
        }
        String token = userInfo.getToken();
        JSONObject wssInfo = SendMessage.getAccessToken(uid, token);
        if (wssInfo.getInteger("ec") != 200) {
            throw new BizzRuntimeException("账号已失效，请重新上号");
        }
        userTask = new UserTask();
        userTask.setCvsType(cvsType);
        userTask.setUid(uid);
        userTask.setAccountId(userInfo.getAccountId());
        userTask.setStatus(1);
        userTask.setSendInterval(sendInterval * 1000);
        userTask.setMessageContent(messageContent);
        userTaskMapper.insert(userTask);
        statusMap.put(uid, 1);
        log.info("【yuni】任务记录添加成功: {}", JSON.toJSONString(userTask));
        taskService.asyncAddTask(addTaskInput);
        // 这里为啥要sleep，是要等异步的方法把图片处理完成
        Thread.sleep(1500);
        return Response.success();
    }

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
}
