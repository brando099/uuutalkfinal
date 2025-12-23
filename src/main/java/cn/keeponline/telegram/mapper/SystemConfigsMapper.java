package cn.keeponline.telegram.mapper;

import cn.keeponline.telegram.entity.SystemConfigs;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemConfigsMapper extends BaseMapper<SystemConfigs> {
    SystemConfigs getByKey(String key);

    int updateByKey(String key, String value);
}
