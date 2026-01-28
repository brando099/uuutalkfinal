package cn.keeponline.telegram.dto.uuudto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchDTO {
    private String uid;
    private String name;
    private String vercode;
    private Integer status;
    private Integer is_destroy;
}
