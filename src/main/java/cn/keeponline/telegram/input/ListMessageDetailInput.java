package cn.keeponline.telegram.input;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ListMessageDetailInput {
    @NotNull(message = "用户id不能为空")
    private Long userId;

    @NotNull(message = "联系人id")
    private Long otherId;

    @NotNull(message = "每页条数不能为空")
    private Integer pageSize;

    @NotNull(message = "页码不能为空")
    private Integer pageNumber;
}
