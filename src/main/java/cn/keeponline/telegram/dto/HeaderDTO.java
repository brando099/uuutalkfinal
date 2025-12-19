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
public class HeaderDTO {
    @ApiModelProperty("好友id")
    private Integer ec;
    private String em;
    private String uid;
    private String remoteid;
    private String cmdtype;

}
