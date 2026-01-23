package cn.keeponline.telegram.entity;

import lombok.Data;

import java.util.Date;

@Data
public class SendRecord {

    private String id;

    private String uid;

    private String groupName;

    private Integer status;

    private String reason;

    private Date createTime;

    private Date modifyTime;

}
