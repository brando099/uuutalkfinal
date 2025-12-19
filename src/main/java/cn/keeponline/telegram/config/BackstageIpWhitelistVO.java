package cn.keeponline.telegram.config;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 后台IP白名单
 *
 * @author TianYang.Pu
 * @version 1.0  ,2019.11.14
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class BackstageIpWhitelistVO {

    /** id */
    private String id;

    /** IP地址 */
   private String ipAddress;

    /** 备注信息 */
   private String detail;

    /** 添加时间 */
   private Date addTime;

    /** 状态 */
    private Boolean status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date modifyDate;

    public Integer no;

}
