package cn.keeponline.telegram.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {
    @ApiModelProperty("联系人id")
    private Long otherId;

    @ApiModelProperty("发送时间")
    private Date sendTime;

    @ApiModelProperty("最后一条消息内容")
    private String lastMessage;
}
