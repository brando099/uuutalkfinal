package cn.keeponline.telegram.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Message {
    private Long id;
    private Long userId;
    private Long otherId;
    private Long messageId;
    private Long fromId;
    private Long peerId;
    private String messageContent;
    private String username;
    private Date sendTime;
    private Date createTime;
    private Integer isDelete;
}
