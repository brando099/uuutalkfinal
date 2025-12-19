package cn.keeponline.telegram.mapper;

import cn.keeponline.telegram.entity.MsgRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

@Repository
public interface MsgRecordMapper extends BaseMapper<MsgRecord> {
    int insertIgnore(MsgRecord msgRecord);
}
