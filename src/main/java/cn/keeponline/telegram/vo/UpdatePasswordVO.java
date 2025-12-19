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
public class UpdatePasswordVO {

    @ApiModelProperty(notes = "旧密码 密码 加密方式:md5(md5(password))", example="9db06bcff9248837f86d1a6bcf41c9e7", required = true)
    private String oldPassword;

    @ApiModelProperty(notes = "新密码 密码 加密方式:md5(md5(password))", example="9db06bcff9248837f86d1a6bcf41c9e7", required = true)
    private String newPassword;

}
