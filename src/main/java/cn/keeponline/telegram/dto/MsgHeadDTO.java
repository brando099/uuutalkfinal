package cn.keeponline.telegram.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MsgHeadDTO {
    private Long offset;
    private Integer msg_type;
    private Integer cvs_type;

}
