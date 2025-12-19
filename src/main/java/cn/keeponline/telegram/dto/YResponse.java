package cn.keeponline.telegram.dto;

import lombok.Data;

@Data
public class YResponse<T> {
    private Integer code;
    private String msg;
    private T data;
}