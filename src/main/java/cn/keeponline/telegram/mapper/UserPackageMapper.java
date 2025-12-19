package cn.keeponline.telegram.mapper;

import cn.keeponline.telegram.entity.UserPackage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPackageMapper extends BaseMapper<UserPackage> {
    UserPackage getByAccountIdAndUid(String accountId, String uid);

    List<UserPackage> listValidByAccountIdAndStatus(String accountId, Integer status);

    List<UserPackage> listByAccountIdAndStatus(String accountId, Integer status);

    UserPackage getByIdAndAccountIdAndStatus(Long id, String accountId, Integer status);

    int updateExpire();

    List<UserPackage> listExpirePackage();

    UserPackage getByUidAndStatus(String uid, Integer status);

    List<UserPackage> listByUidsAndStatus(List<String> uids, Integer status);
}
