package cn.keeponline.telegram.input;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class SysUserUpdateInput {

    @ApiModelProperty("主键")
    @NotNull(message = "主键不能为空")
    private String id;

    @ApiModelProperty("密码（两次md5的值）")
    @NotEmpty(message = "密码不能为空")
    private String password;

    @ApiModelProperty("状态")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @ApiModelProperty("谷歌令牌")
    private String secretKey;

    @ApiModelProperty("谷歌令牌是否启用")
    private Boolean googleAuthEnabled;
}
