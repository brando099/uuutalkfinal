package cn.keeponline.telegram.dto;

import lombok.Data;

@Data
public class WSResponse<T> {
    private HeaderDTO header;
    private T body;
}