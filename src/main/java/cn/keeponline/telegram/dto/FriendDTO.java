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
public class FriendDTO {
    @ApiModelProperty("好友id")
    private String remote_uid;

    @ApiModelProperty("昵称")
    private String nickname;
}
