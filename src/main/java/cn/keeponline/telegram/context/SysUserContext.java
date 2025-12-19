package cn.keeponline.telegram.context;

import cn.keeponline.telegram.config.Constants;
import cn.keeponline.telegram.entity.SysUser;
import cn.keeponline.telegram.exception.BizzRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@Component
public class SysUserContext {

    @Autowired
    private HttpServletRequest request;

    public SysUser getRequestUser() {
        SysUser user = (SysUser) request.getAttribute(Constants.REQUEST_USER);
        if (Objects.isNull(user)) {
            throw new BizzRuntimeException("找不到用户");
        }
        return user;
    }

    public String getRequestSysUserId() {
        String userId = (String) request.getAttribute(Constants.REQUEST_SYS_USER_ID);
        if (Objects.isNull(userId)) {
            throw new BizzRuntimeException("找不到用户");
        }
        return userId;
    }

    public String getAccountId() {
        String userId = (String) request.getAttribute(Constants.REQUEST_OUT_ID);
        if (Objects.isNull(userId)) {
            throw new BizzRuntimeException("找不到用户");
        }
        return userId;
    }

}
