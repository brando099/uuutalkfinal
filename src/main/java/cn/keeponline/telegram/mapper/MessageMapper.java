package cn.keeponline.telegram.mapper;

import cn.keeponline.telegram.entity.Message;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageMapper extends BaseMapper<Message> {
    /**
     * 通过userId, 查询用户所有聊过天的人
     * @param userId 用户id
     * @return 用户所有聊过天的人
     */
    List<Message> listByUserId(Long userId);

    /**
     * 通过用户id和otherId查询消息列表
     */
    List<Message> listByUserIdAndOtherId(@Param("userId") Long userId, @Param("otherId") Long otherId);


    /**
     * 通过用户id和otherId查询消息列表，limit 最新的一条数据
     */
    Message listByUserIdAndOtherIdLimit1(@Param("userId") Long userId, @Param("otherId") Long otherId);

    List<Message> listByUserId2(String messageContent);
}
