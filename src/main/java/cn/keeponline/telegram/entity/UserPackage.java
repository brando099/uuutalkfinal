package cn.keeponline.telegram.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class UserPackage {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String uid;
    private String accountId;
    private Integer status;
    private Date expireTime;
    private Date createTime;
    private Date modifyTime;
}
