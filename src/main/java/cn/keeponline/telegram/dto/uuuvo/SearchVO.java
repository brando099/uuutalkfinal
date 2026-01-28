package cn.keeponline.telegram.dto.uuuvo;

import cn.keeponline.telegram.dto.uuudto.SearchDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchVO {
    private Integer exist;
    private SearchDTO data;
}
