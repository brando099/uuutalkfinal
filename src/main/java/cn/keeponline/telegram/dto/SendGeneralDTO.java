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
public class SendGeneralDTO {
    @ApiModelProperty("id")
    private String id;

    @ApiModelProperty("名称")
    private String name;
}
