package cn.keeponline.telegram.service;

import cn.keeponline.telegram.entity.UserInfo;

import java.util.List;

/**
 * 运营平台用户(SysUser)表服务接口
 *
 * @author makejava
 * @since 2021-03-30 17:03:58
 */
public interface UserInfoService {
    List<UserInfo> listByAccountId(String accountId) throws Exception;

    void delete(String id);
}