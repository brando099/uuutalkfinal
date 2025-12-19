package cn.keeponline.telegram.input;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ListSysUserInput {
    @ApiModelProperty("用户名（用于搜索）")
    private String username;

    @NotNull(message = "页码不能为空")
    private Integer pageNumber;

    @NotNull(message = "每页条数不能为空")
    private Integer pageSize;

}
