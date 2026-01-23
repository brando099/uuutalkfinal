package cn.keeponline.telegram.service.impl;

import cn.keeponline.telegram.component.AsyncComponent;
import cn.keeponline.telegram.context.SysUserContext;
import cn.keeponline.telegram.entity.UserInfo;
import cn.keeponline.telegram.entity.UserPackage;
import cn.keeponline.telegram.entity.UserTask;
import cn.keeponline.telegram.exception.BizzRuntimeException;
import cn.keeponline.telegram.mapper.UserInfoMapper;
import cn.keeponline.telegram.mapper.UserPackageMapper;
import cn.keeponline.telegram.mapper.UserTaskMapper;
import cn.keeponline.telegram.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private SysUserContext sysUserContext;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserTaskMapper userTaskMapper;
    @Qualifier("userPackageMapper")
    @Autowired
    private UserPackageMapper userPackageMapper;
    @Autowired
    private AsyncComponent asyncComponent;

    @Override
    public List<UserInfo> listByAccountId(String accountId) throws Exception {
        List<UserInfo> userInfos = userInfoMapper.listByAccountId(accountId);
        if (!userInfos.isEmpty()) {
//            asyncComponent.syncCountInfo(userInfos);
        }
        for (UserInfo userInfo : userInfos) {
            String taskStatus = "未配置";
            String userTaskStatus = "未配置";
            String uid = userInfo.getUid();
            UserTask userTask = userTaskMapper.getByAccountIdAndUidAndCvsType(accountId, uid, 2);
            taskStatus = getTaskStatus(taskStatus, userTask);
            userInfo.setTaskStatus(taskStatus);
            userTask = userTaskMapper.getByAccountIdAndUidAndCvsType(accountId, uid, 1);
            userTaskStatus = getTaskStatus(userTaskStatus, userTask);
            userInfo.setUserTaskStatus(userTaskStatus);
            Long packageId = userInfo.getPackageId();
            UserPackage userPackage = userPackageMapper.selectById(packageId);
            userInfo.setExpireTime(userPackage.getExpireTime());
        }
        return userInfos;
    }

    private String getTaskStatus(String taskStatus, UserTask userTask) {
        if (userTask != null) {
            Integer status = userTask.getStatus();
            if (status == 1) {
                taskStatus = "发送中";
            } else if (status == 2) {
                taskStatus = "任务停止";
            } else if (status == 0) {
                taskStatus = "初始化中";
            }
        }
        return taskStatus;
    }

    @Override
    public void delete(String id) {
        UserInfo userInfo = userInfoMapper.selectById(id);
        if (userInfo == null) {
            throw new BizzRuntimeException("账号不存在");
        }
        String uid = userInfo.getUid();
        String accountId = sysUserContext.getAccountId();

        UserTask userTask = userTaskMapper.getByAccountIdAndUidAndCvsType(accountId, uid, null);
        if (userTask != null) {
            throw new BizzRuntimeException("账号存在运行中的任务，停止任务后再删除");
        }
        userInfoMapper.deleteById(id);

        Long packageId = userInfo.getPackageId();
        UserPackage userPackage = userPackageMapper.selectById(packageId);
        userPackage.setStatus(0);

        if (userPackageMapper.updateById(userPackage) == 1) {
            log.info("修改套餐状态成功");
        }
    }
}
