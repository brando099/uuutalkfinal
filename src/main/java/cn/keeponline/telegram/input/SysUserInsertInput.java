package cn.keeponline.telegram.input;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class SysUserInsertInput {
    @ApiModelProperty("用户名")
    @NotEmpty(message = "用户名不能为空")
    private String username;

    @ApiModelProperty("密码（两次md5的值）")
    @NotEmpty(message = "密码不能为空")
    private String password;

    // 套餐有效期
    @NotNull
    private Integer validDays;

    // 套餐数量
    @NotNull
    private Integer packageCount;

}
