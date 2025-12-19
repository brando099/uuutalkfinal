package cn.keeponline.telegram.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class SendRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String uid;

    private String groupName;

    private Integer status;

    private String reason;

    private Date createTime;

    private Date modifyTime;

}
