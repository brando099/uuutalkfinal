package cn.keeponline.telegram.vo;

import cn.keeponline.telegram.dto.MessageDetailPageDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class MessageDetailPageVO {
    @ApiModelProperty("详情页消息列表")
    private List<MessageDetailPageDTO> list;
}
