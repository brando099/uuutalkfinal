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
public class UUUFriendDTO {
    @ApiModelProperty("uid")
    private String uid;

    private String name;


}
