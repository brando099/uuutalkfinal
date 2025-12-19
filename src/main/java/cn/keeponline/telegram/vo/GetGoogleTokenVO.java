package cn.keeponline.telegram.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class GetGoogleTokenVO {
    @ApiModelProperty("二维码")
    private String qrCode;

    @ApiModelProperty("秘钥")
    private String secretKey;
}
