package cn.keeponline.telegram.entity;

import lombok.Data;

import java.util.Date;

@Data
public class PictureInfo {
    private String id;
    private String fileName;
    private Long fileSize;
    private Date createTime;
    private Date modifyTime;

}
