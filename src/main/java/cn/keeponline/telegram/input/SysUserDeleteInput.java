package cn.keeponline.telegram.input;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SysUserDeleteInput {
    @ApiModelProperty("主键")
    @NotNull(message = "主键不能为空")
    private String id;
}
