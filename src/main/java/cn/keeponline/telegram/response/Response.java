package cn.keeponline.telegram.response;

import cn.keeponline.telegram.utils.Json2;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Wind
 * @since 2019-06-05 20:31
 */
@Data
@NoArgsConstructor
@ToString
public class Response<T> {
    /**
     * 返回编码 0:成功  -1：失败
     */
    private int code;
    /**
     * 返回消息
     */
    private String message;
    /**
     * 返回数据
     */
    private T data;
    /**
     * 成功失败状态码
     */
    private boolean success;

    /**
     * 通用请求成功
     *
     * @return
     */
    public static Response success() {
        Response response = new Response();
        response.setCode(ResponseEnum.RESULT_SUCCESS.getCode());
        response.setMessage(ResponseEnum.RESULT_SUCCESS.getMessage());
        response.setSuccess(true);
        return response;
    }

    /**
     * 通用请求成功
     *
     * @param data 数据对象
     * @return
     */
    public static <T> Response<T> success(T data) {
        Response<T> response = new Response<T>();
        response.setCode(ResponseEnum.RESULT_SUCCESS.getCode());
        response.setMessage(ResponseEnum.RESULT_SUCCESS.getMessage());
        response.setData(data);
        response.setSuccess(true);
        return response;
    }

    /**
     * 通用请求失败方法
     *
     * @param responseEnum 响应枚举类
     * @return
     */
    public static Response fail(ResponseEnum responseEnum) {
        Response response = new Response();
        response.setCode(responseEnum.getCode());
        response.setMessage(responseEnum.getMessage());
        response.setSuccess(false);
        return response;
    }

    /**
     * 通用请求失败方法
     *
     * @param code    错误码
     * @param message 提示信息
     * @return
     */
    public static Response fail(int code, String message) {
        Response response = new Response();
        response.setCode(code);
        response.setMessage(message);
        response.setSuccess(false);
        return response;
    }

    /**
     * 通用请求失败方法
     *
     * @param exception    错误码
     * @return
     */
    public static Response fail(Exception exception) {
        Response response = new Response();
        response.setCode(-1);
        response.setMessage(exception.getMessage());
        response.setSuccess(false);
        return response;
    }

    /**
     * 通用请求失败方法
     *
     * @param responseEnum 错误类型
     * @param data
     * @return
     */
    public static Response fail(ResponseEnum responseEnum, Object data) {
        Response response = fail(responseEnum);
        response.setData(data);
        response.setSuccess(false);
        return response;
    }

    public static String toSuccJson(ResponseEnum responseEnum) {
        return Json2.toJson(success(responseEnum));
    }

    public static String toFailJson(ResponseEnum responseEnum) {
        return Json2.toJson(fail(responseEnum));
    }

    public String toJson() {
        return Json2.toJson(this);
    }

    public static <T> Response<T> fromJson(String json) {
        return Json2.fromJson(json, Response.class);
    }
}
