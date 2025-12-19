package cn.keeponline.telegram.controller;

import cn.keeponline.telegram.context.SysUserContext;
import cn.keeponline.telegram.entity.UserInfo;
import cn.keeponline.telegram.response.Response;
import cn.keeponline.telegram.service.UserInfoService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/userInfo")
@Api(tags = "会员管理")
@Slf4j
public class UserInfoController extends ControllerBase {

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private SysUserContext sysUserContext;

    @RequestMapping("/listByAccountId")
    public Response listByAccountId() throws Exception {
        String outId = sysUserContext.getAccountId();
        List<UserInfo> userInfos = userInfoService.listByAccountId(outId);
        return Response.success(userInfos);
    }

    @RequestMapping("/delete")
    public Response delete(String id)  {
        userInfoService.delete(id);
        return Response.success();
    }




}
