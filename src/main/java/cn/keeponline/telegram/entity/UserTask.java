package cn.keeponline.telegram.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class UserTask {
    @TableId(value = "id", type = IdType.UUID)
    private String id;
    private Integer type;
    private Integer cvsType;
    private String uid;
    private String accountId;
    // 任务状态(0 异常 1 正常 2 停止)
    private Integer status;
    private Integer isDelete;
    private Integer sendInterval;
    private String messageContent;
    private String md5;
    private String fileName;
    private Long fileSize;
    private Date createTime;
    private Date modifyTime;
}
