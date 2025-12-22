package cn.keeponline.telegram.dto.uuudto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UUUGroupDTO {
    @ApiModelProperty("group_no")
    private String group_no;

    private String group_type;

    private String name;

    private Integer forbidden;

    private Integer role;
}
