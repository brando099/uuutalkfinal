package cn.keeponline.telegram.input;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
// 获取用户列表入参
public class ListUsersInput {
    @ApiModelProperty(value = "用户id or 用户名，都使用这个字段，通过用户id搜索")
    private String userId;

    @NotNull(message = "每页条数不能为空")
    private Integer pageSize;

    @NotNull(message = "页码不能为空")
    private Integer pageNumber;
}
