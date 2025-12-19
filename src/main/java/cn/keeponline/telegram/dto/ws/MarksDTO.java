package cn.keeponline.telegram.dto.ws;

import lombok.Data;

@Data
public class MarksDTO {
    private String id;
    private Long offset;
    private Integer type;
}
