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
public class MessageDetailPageDTO {
    @ApiModelProperty("消息内容")
    private String messageContent;

    @ApiModelProperty("发送时间")
    private Date sendTime;

    @ApiModelProperty("发送方id")
    private Long fromId;
}
