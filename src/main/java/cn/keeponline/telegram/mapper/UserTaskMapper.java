package cn.keeponline.telegram.mapper;

import cn.keeponline.telegram.entity.UserTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserTaskMapper extends BaseMapper<UserTask> {
    List<UserTask> listByUidAndStatus(String uid, Integer status);

    UserTask getByUidAndCvsTypeAndStatus(String uid, Integer cvsType, Integer status);

    List<UserTask> getByStatus(Integer status);

    List<UserTask> listByUid(String uid);

    int deleteByUidsAndCvsType(List<String> uids, Integer cvsType);

    int updateFrequency(Integer sendInterval, List<String> uids, Integer cvsType);

    UserTask getByAccountIdAndUidAndCvsType(String accountId, String uid, Integer cvsType);
}
