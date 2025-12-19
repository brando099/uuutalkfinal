package cn.keeponline.telegram.config;

import com.google.common.collect.Lists;

import java.util.Date;
import java.util.List;

/**
 * @author wind
 * @since 2019-06-13 15:06
 */
public class Constants {

    /**
     * 资源映射路径 前缀
     */
    public static final String RESOURCE_PREFIX = "/profile";

    /**
     * lock tag
     */
    public static final String LOCK_TAG = "lockTag";

    /**
     * jwt data
     */
    public static final String REQUEST_USER_ID = "userId";

    /**
     * jwt data
     */
    public static final String REQUEST_SYS_USER_ID = "sysUserId";

    /**
     * jwt data
     */
    public static final String REQUEST_OUT_ID = "outId";

    /**
     * request user
     */
    public static final String REQUEST_USER = "requestUser";

    /**
     * request header token key
     */
    public static final String REQUEST_HEADER_AUTH = "Authorization";

    /**
     * request header token key
     */
    public static final String REQUEST_HEADER_SIGNATURE = "Signature";

    public static final String API_ADDRESS = "https://api.uneed.com";

    /**
     * 跨域请求头
     */
    public static final List<String> ALLOW_HEADERS = Lists.newArrayList(
            "Origin"
            , "X-Requested-With"
            , "Content-Type"
            , "Accept"
            , Constants.REQUEST_HEADER_AUTH
    );
}
