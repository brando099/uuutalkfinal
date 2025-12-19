package cn.keeponline.telegram.entity;

import lombok.Data;

import java.util.Date;

@Data
public class User {
    private Long id;
    private Long userId;
    private String username;
    private Date createTime;
}
