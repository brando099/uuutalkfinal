package cn.keeponline.telegram.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class MsgRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String msgContent;

    private Integer msgType;

    private Date date;

    private String uid;

    private String gid;

    private Date createTime;

    private Date modifyTime;

}
