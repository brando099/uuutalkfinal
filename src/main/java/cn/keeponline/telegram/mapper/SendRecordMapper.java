package cn.keeponline.telegram.mapper;

import cn.keeponline.telegram.entity.SendRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SendRecordMapper extends BaseMapper<SendRecord> {
    List<SendRecord> listByUid(String uid);

    int deleteSendRecord(String createTime);
}
