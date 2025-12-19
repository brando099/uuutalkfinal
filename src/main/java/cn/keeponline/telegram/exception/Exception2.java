package cn.keeponline.telegram.exception;

import cn.keeponline.telegram.response.ResponseEnum;

/**
 * @author shaoshuai
 * @since 2018-05-23 11:11
 */
public class Exception2 {

    public static void throwBizzEx(String message) {
        throw new BizzRuntimeException(message, 500);
    }

    public static void throwBizzEx(ResponseEnum responseEnum) {
        throw new BizzRuntimeException(responseEnum.getMessage(), responseEnum.getCode());
    }

    public static void throwBizzEx(String message, Integer code) {
        throw new BizzRuntimeException(message, code);
    }

    private Exception2() {
        super();
    }

}
