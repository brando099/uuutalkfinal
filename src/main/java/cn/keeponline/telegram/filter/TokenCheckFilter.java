package cn.keeponline.telegram.filter;

import cn.keeponline.telegram.config.Constants;
import cn.keeponline.telegram.entity.SysUser;
import cn.keeponline.telegram.response.Response;
import cn.keeponline.telegram.response.ResponseEnum;
import cn.keeponline.telegram.service.SysUserService;
import cn.keeponline.telegram.utils.Jwt2;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@Order(3)
@WebFilter(filterName = "TokenCheckFilter", urlPatterns = {"/*"})
public class TokenCheckFilter extends BaseFilter implements Filter {

    @Autowired
    private Jwt2 jwt2;

    @Autowired
    private SysUserService sysUserService;

    @Override
    public void init(FilterConfig filterConfig) {
        super.init(filterConfig, whiteList(), signList());
        log.info("TokenCheckFilter init...");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
//        WrappedHttpServletRequest requestWrapper = new WrappedHttpServletRequest(request);

//        log.info("TokenCheckFilter Url: " + request.getRequestURI());
        if (isRequestExclude(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        // 验证 Jwt Token
        String token = request.getHeader(Constants.REQUEST_HEADER_AUTH);
        if (Strings.isNullOrEmpty(token)) {
            redirectToFail(response, ResponseEnum.RESULT_USER_TOKEN_MISSING);
            return;
        }
        // 解密token
        Jwt2.JwtPayload jwtPayload = jwt2.decode(token);
        if (Objects.isNull(jwtPayload)) {
            redirectToFail(response, ResponseEnum.RESULT_TOKEN_INVALID);
            return;
        }
        String userOutId = jwtPayload.getCustomPayloadValue(Constants.REQUEST_OUT_ID);
        // redis验证token
        SysUser sysUser = sysUserService.getByOutId(userOutId);
        if (sysUser == null) {
            redirectToFail(response, ResponseEnum.USER_NOT_EXISTS);
            return;
        }
        request.setAttribute(Constants.REQUEST_HEADER_AUTH, token);
        request.setAttribute(Constants.REQUEST_SYS_USER_ID, sysUser.getId());
//        log.info("TokenCheckFilter doFilter...{}", sysUser.getId());
        request.setAttribute(Constants.REQUEST_OUT_ID, sysUser.getOutId());
        request.setAttribute(Constants.REQUEST_USER, sysUser);
        //执行
        filterChain.doFilter(request, response);
    }

    private List<String> whiteList() {
        // 添加白名单URL
        return Lists.newArrayList(
                "sysUser/login",
                "message/insert",
                "picture/upload",
                "/v1/dfapi/add",
                "/v1/dfapi/query_order",
                "/getWS",
                "/getSmata",
                "test3",
                "/task/testRestart",
                "test2",
                "test",
                "getInfo",
                "login",
                "getRouters",
                "task/addTask",
                "task/getSocketMapInfo"
        );
    }

    private List<String> signList() {
        // 添加签名URL
        return Collections.emptyList();
    }

    private void redirectToFail(HttpServletResponse response, ResponseEnum responseEnum) {
        try {
            response.setContentType("application/json; charset=utf-8");
            response.setCharacterEncoding("UTF-8");

            Response result = Response.fail(responseEnum);
            String json = result.toJson();
            OutputStream out = response.getOutputStream();
            out.write(json.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (Exception ignored) { }
    }

    @Override
    public void destroy() {
        log.info("TokenCheckFilter destroy...");
    }
}
