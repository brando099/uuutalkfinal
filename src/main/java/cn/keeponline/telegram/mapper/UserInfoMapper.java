package cn.keeponline.telegram.mapper;

import cn.keeponline.telegram.entity.UserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserInfoMapper extends BaseMapper<UserInfo> {
    UserInfo getByUid(String uid);

    UserInfo getByAccountIdAndUid(String accountId, String uid);

    UserInfo getByUuid(String uuid);

    List<UserInfo> listByAccountId(String accountId);

    List<UserInfo> listByUids(List<String> uids);
}
