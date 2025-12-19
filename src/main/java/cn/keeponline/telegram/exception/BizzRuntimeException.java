package cn.keeponline.telegram.exception;

import cn.keeponline.telegram.response.ResponseEnum;

/**
 * @author shaoshuai
 * @since 2018-05-23 11:11
 */
public class BizzRuntimeException extends RuntimeException {

    /**
     * 异常编码
     */
    private final Integer code;

    public BizzRuntimeException(String message) {
        super(message);
        this.code = 500;
    }

    public BizzRuntimeException(String message, Integer code) {
        super(message);
        this.code = code;
    }

    public BizzRuntimeException(ResponseEnum responseEnum) {
        super(responseEnum.getMessage());
        this.code = responseEnum.getCode();
    }

    public BizzRuntimeException(String message, Integer code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

}
