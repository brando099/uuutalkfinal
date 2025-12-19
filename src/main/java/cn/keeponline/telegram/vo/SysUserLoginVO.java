package cn.keeponline.telegram.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysUserLoginVO {

    @ApiModelProperty(notes = "用户名", example="root", required = true)
    private String username;

    @ApiModelProperty(notes = "密码 加密方式:md5(md5(password))", example="9db06bcff9248837f86d1a6bcf41c9e7", required = true)
    private String password;

    @ApiModelProperty(notes = "google令牌验证码", example="123456")
    private Long googleToken;

}
