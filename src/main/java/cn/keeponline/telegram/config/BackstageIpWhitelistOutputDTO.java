package cn.keeponline.telegram.config;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * @author TianYang.Pu
 * @version 1.0  ,2019.11.14
 */
@Data
public class BackstageIpWhitelistOutputDTO {

    private String id;

    @NotBlank(message = "IP地址不能为空")
    @ApiModelProperty("IP地址")
    private String ipAddress;


    @ApiModelProperty("备注信息")
    private String detail;

    /** 添加时间 */
    private Date createDate;

    /** 状态 */
    private boolean status;

    private Integer no;

    private Date modifyDate;

}
