package cn.keeponline.telegram.mapper;

import cn.keeponline.telegram.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMapper extends BaseMapper<User> {
    /**
     * 分页查询用户列表
     */
    List<User> listUsers(String userId);
}
