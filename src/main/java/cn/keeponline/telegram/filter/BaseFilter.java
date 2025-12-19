package cn.keeponline.telegram.filter;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.assertj.core.util.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
 * 过滤器基类
 * @author sanzang
 */
public abstract class BaseFilter {

    private static final Logger logger = LoggerFactory.getLogger(BaseFilter.class);
    private static final String FILTER_PARAM_EXCLUDE = "excludes";

    /**
     * 不需要拦截的URI模式，以正则表达式表示
     */
    private String excludeExp;

    /**
     * URL白名单列表
     */
    private Set<String> whiteList;

    /**
     * 签名接口URL列表
     */
    private Set<String> signList;

    protected void init(FilterConfig filterConfig, List<String> whiteList,
                        List<String> signList) {
        this.excludeExp = filterConfig.getInitParameter(FILTER_PARAM_EXCLUDE);
        this.whiteList = Sets.newLinkedHashSet(
                ".jpg",
                ".png",
                ".jpeg",
                ".gif",
                "swagger-resources",
                "swagger-ui",
                "v2/api-docs"
        );
        this.signList = Sets.newLinkedHashSet();
        this.whiteList.addAll(ImmutableSet.copyOf(whiteList));
        this.signList.addAll(ImmutableSet.copyOf(signList));
    }

    private boolean isWhiteList(HttpServletRequest request) {
        String contextPath = request.getRequestURI().trim();
        return Iterables.any(whiteList, contextPath::contains);
    }

    protected boolean isSignList(HttpServletRequest request) {
        String contextPath = request.getContextPath().trim();
        return Iterables.any(signList, contextPath::contains);
    }

    boolean isRequestExclude(HttpServletRequest request) {
        // 白名单请求不需要拦截
        if (isWhiteList(request)) {
            return true;
        }

        // 未设置exclude，则请求会被拦截
        if (Strings.isNullOrEmpty(excludeExp)) {
            return false;
        }

        // 去掉域名和contextPath后的URL路径
        String path = request.getServletPath();
        boolean isExcluded = path.matches(excludeExp);

        if (isExcluded) {
            logger.debug("Request Path：{} is excluded.", path);
        }

        return isExcluded;
    }

}
