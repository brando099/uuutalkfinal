package cn.keeponline.telegram.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupDTO {
    @ApiModelProperty("群组id")
    private String gid;

    @ApiModelProperty("群组名称")
    private String name;
}
