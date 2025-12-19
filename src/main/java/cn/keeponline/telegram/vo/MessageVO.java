package cn.keeponline.telegram.vo;

import cn.keeponline.telegram.dto.MessageDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class MessageVO {
    @ApiModelProperty("总条数")
    private Integer total;

    @ApiModelProperty("查询页的数据")
    private List<MessageDTO> list;
}
