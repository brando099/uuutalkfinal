package cn.keeponline.telegram.controller;

import cn.keeponline.telegram.entity.SendRecord;
import cn.keeponline.telegram.mapper.SendRecordMapper;
import cn.keeponline.telegram.response.Response;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sendRecord")
@Api(tags = "会员管理")
@Slf4j
public class SendRecordController extends ControllerBase {

    private static final String SEND_RECORD_KEY_PREFIX = "sendRecord:";

    @Autowired
    @Qualifier("redisTemplate1")
    private RedisTemplate<String, Object> redisTemplate;

    @RequestMapping("/listByUid")
    public Response listByAccountId(String uid) {
        String key = SEND_RECORD_KEY_PREFIX + uid;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        List<SendRecord> list = new ArrayList<>();
        for (Object value : entries.values()) {
            list.add((SendRecord) value);
        }
        list.sort(Comparator.comparing(SendRecord::getCreateTime).reversed());
        return Response.success(list);
    }
}
