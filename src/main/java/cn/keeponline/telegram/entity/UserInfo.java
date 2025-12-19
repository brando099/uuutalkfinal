package cn.keeponline.telegram.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class UserInfo {
    @TableId(value = "id", type = IdType.UUID)
    private String id;
    private String uid;
    private String accountId;
    private Long packageId;
    private String token;
    private String uuid;
    private String nickname;
    private String mobile;
    private Integer groupSize;
    private Integer friendSize;
    private Integer status;
    private Date createTime;
    private Date modifyTime;

    // 任务状态
    @TableField(exist = false)
    private String taskStatus;

    // 好友任务状态
    @TableField(exist = false)
    private String userTaskStatus;

    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expireTime;

}
