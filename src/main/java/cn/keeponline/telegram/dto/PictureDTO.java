package cn.keeponline.telegram.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PictureDTO {
    private String thumb_url;
    private String preview_url;
    private String origin_url;
    private String full_url;
}
