package cn.keeponline.telegram.response;

/**
 * @author Wind
 * @since 2019-06-05 20:30
 */
public enum ResponseEnum {

    /**
     * 通用业务段
     */
    RESULT_SUCCESS(200, "成功"),
    RESULT_NOT_LOGGED_IN(401, "用户未登录"),
    RESULT_ACCESS_DENIED(402, "访问被拒绝"),
    RESULT_PERMISSION_UNAUTHORIZED(403, "权限不足"),
    RESULT_RESOURCE_NOT_FOUND(404, "目标资源不存在"),
    RESULT_ERROR(500, "服务器内部错误"),

    /**
     * 认证相关业务 1xxx
     */
    RESULT_CAPTCHA_CODE_ERROR(1000, "验证码错误"),
    RESULT_ACCOUNT_LOCKED(1001, "帐号被限制登录，请%s分钟后重试"),
    RESULT_USERNAME_PASSWORD_INCORRECT(1002, "账号或密码不正确"),
    RESULT_AUTHENTICATION_EXCEPTION(1003, "登陆认证异常"),
    RESULT_ACCOUNT_BANNED(1004, "账户已被停用"),
    RESULT_USER_TYPE_INCORRECT(1005, "用户类型异常"),
    RESULT_TOKEN_INVALID(1006, "TOKEN无效或已过期"),
    RESULT_TOKEN_PARAM_INVALID(1007, "TOKEN参数无效"),
    RESULT_USER_TOKEN_MISSING(1008, "TOKEN缺失"),
    RESULT_USER_SIGNATURE_ERROR(1009, "SIGNATURE缺失"),
    RESULT_USER_REQUIRE_GOOGLE_AUTH(1010, "您已经开启Google令牌, 请输入令牌验证码"),
    FREQUENT_OPERATION(1011,"操作频繁"),
    ADMIN_CANNOT_DELETE(1012,"admin用户不能删除"),
    USER_NOT_EXISTS(1013,"用户不存在"),



    /**
     * 服务异常
     */
    RESULT_REDIS_SAVE_ERROR(10000, "Redis保存数据异常"),

    /**
     * eth调用异常
     */
    ETH_COMMONS_ERROR(20000, "ETH调用异常"),

    ;


    private Integer code;
    private String message;

    ResponseEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
