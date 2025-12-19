package cn.keeponline.telegram.controller;

import cn.keeponline.telegram.entity.SysUser;
import cn.keeponline.telegram.exception.BizzRuntimeException;
import cn.keeponline.telegram.input.ListSysUserInput;
import cn.keeponline.telegram.input.SysUserDeleteInput;
import cn.keeponline.telegram.input.SysUserInsertInput;
import cn.keeponline.telegram.input.SysUserUpdateInput;
import cn.keeponline.telegram.response.Response;
import cn.keeponline.telegram.response.ResponseEnum;
import cn.keeponline.telegram.service.SysUserService;
import cn.keeponline.telegram.vo.GetGoogleTokenVO;
import cn.keeponline.telegram.vo.SysUserLoginVO;
import cn.keeponline.telegram.vo.UpdatePasswordVO;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping
@Slf4j
@Api(tags = {"后台用户管理"})
public class SysUserController extends ControllerBase {

    @Autowired
    private SysUserService sysUserService;

    @PostMapping("/login")
    @ApiOperation(value = "登录", httpMethod = "POST")
//    @CrossOrigin(origins = "*", allowCredentials = "true")
    public Response<String> login(@RequestBody @Valid SysUserLoginVO login) {
        return sysUserService.login(login);
    }

    @RequestMapping("/logout")
    @ApiOperation(value = "登录", httpMethod = "POST")
    public Response<String> logout(@RequestBody @Valid SysUserLoginVO login) {
        return Response.success();
    }

    @GetMapping("/getInfo")
    @ApiOperation(value = "登录", httpMethod = "POST")
    public String getInfo() {
        return "{\n" +
                "    \"msg\": \"操作成功\",\n" +
                "    \"code\": 200,\n" +
                "    \"permissions\": [\n" +
                "        \"*:*:*\"\n" +
                "    ],\n" +
                "    \"roles\": [\n" +
                "        \"admin\"\n" +
                "    ],\n" +
                "    \"isDefaultModifyPwd\": false,\n" +
                "    \"isPasswordExpired\": false,\n" +
                "    \"user\": {\n" +
                "        \"createBy\": \"admin\",\n" +
                "        \"createTime\": \"2025-08-26 16:01:39\",\n" +
                "        \"updateBy\": null,\n" +
                "        \"updateTime\": null,\n" +
                "        \"remark\": \"管理员\",\n" +
                "        \"params\": {\n" +
                "            \"@type\": \"java.util.HashMap\"\n" +
                "        },\n" +
                "        \"userId\": 1,\n" +
                "        \"deptId\": null,\n" +
                "        \"userName\": \"admin\",\n" +
                "        \"nickName\": \"若依\",\n" +
                "        \"email\": \"ry@163.com\",\n" +
                "        \"phonenumber\": \"15888888888\",\n" +
                "        \"sex\": \"1\",\n" +
                "        \"avatar\": null,\n" +
                "        \"password\": \"$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2\",\n" +
                "        \"status\": \"0\",\n" +
                "        \"delFlag\": \"0\",\n" +
                "        \"loginIp\": \"127.0.0.1\",\n" +
                "        \"loginDate\": \"2025-10-31T16:07:18.000+08:00\",\n" +
                "        \"pwdUpdateDate\": \"2025-08-26T16:01:39.000+08:00\",\n" +
                "        \"dept\": null,\n" +
                "        \"roles\": [\n" +
                "            {\n" +
                "                \"createBy\": null,\n" +
                "                \"createTime\": null,\n" +
                "                \"updateBy\": null,\n" +
                "                \"updateTime\": null,\n" +
                "                \"remark\": null,\n" +
                "                \"params\": {\n" +
                "                    \"@type\": \"java.util.HashMap\"\n" +
                "                },\n" +
                "                \"roleId\": 1,\n" +
                "                \"roleName\": \"超级管理员\",\n" +
                "                \"roleKey\": \"admin\",\n" +
                "                \"roleSort\": 1,\n" +
                "                \"dataScope\": \"1\",\n" +
                "                \"menuCheckStrictly\": false,\n" +
                "                \"deptCheckStrictly\": false,\n" +
                "                \"status\": \"0\",\n" +
                "                \"delFlag\": null,\n" +
                "                \"flag\": false,\n" +
                "                \"menuIds\": null,\n" +
                "                \"deptIds\": null,\n" +
                "                \"permissions\": null,\n" +
                "                \"admin\": true\n" +
                "            }\n" +
                "        ],\n" +
                "        \"roleIds\": null,\n" +
                "        \"postIds\": null,\n" +
                "        \"roleId\": null,\n" +
                "        \"admin\": true\n" +
                "    }\n" +
                "}";
    }

    @GetMapping("/getRouters")
    @ApiOperation(value = "登录", httpMethod = "POST")
    public String getRouters() {
        return "{\n" +
                "    \"msg\": \"操作成功\",\n" +
                "    \"code\": 200,\n" +
                "    \"data\": [\n" +
                "        {\n" +
                "            \"name\": \"System\",\n" +
                "            \"path\": \"/system\",\n" +
                "            \"hidden\": false,\n" +
                "            \"redirect\": \"noRedirect\",\n" +
                "            \"component\": \"Layout\",\n" +
                "            \"alwaysShow\": true,\n" +
                "            \"meta\": {\n" +
                "                \"title\": \"系统管理\",\n" +
                "                \"icon\": \"system\",\n" +
                "                \"noCache\": false,\n" +
                "                \"link\": null\n" +
                "            },\n" +
                "            \"children\": [\n" +
                "                {\n" +
                "                    \"name\": \"User\",\n" +
                "                    \"path\": \"user\",\n" +
                "                    \"hidden\": false,\n" +
                "                    \"component\": \"system/user/index\",\n" +
                "                    \"meta\": {\n" +
                "                        \"title\": \"用户管理\",\n" +
                "                        \"icon\": \"user\",\n" +
                "                        \"noCache\": false,\n" +
                "                        \"link\": null\n" +
                "                    }\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"Role\",\n" +
                "                    \"path\": \"role\",\n" +
                "                    \"hidden\": false,\n" +
                "                    \"component\": \"system/role/index\",\n" +
                "                    \"meta\": {\n" +
                "                        \"title\": \"角色管理\",\n" +
                "                        \"icon\": \"peoples\",\n" +
                "                        \"noCache\": false,\n" +
                "                        \"link\": null\n" +
                "                    }\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"Menu\",\n" +
                "                    \"path\": \"menu\",\n" +
                "                    \"hidden\": false,\n" +
                "                    \"component\": \"system/menu/index\",\n" +
                "                    \"meta\": {\n" +
                "                        \"title\": \"菜单管理\",\n" +
                "                        \"icon\": \"tree-table\",\n" +
                "                        \"noCache\": false,\n" +
                "                        \"link\": null\n" +
                "                    }\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"Dept\",\n" +
                "                    \"path\": \"dept\",\n" +
                "                    \"hidden\": false,\n" +
                "                    \"component\": \"system/dept/index\",\n" +
                "                    \"meta\": {\n" +
                "                        \"title\": \"部门管理\",\n" +
                "                        \"icon\": \"tree\",\n" +
                "                        \"noCache\": false,\n" +
                "                        \"link\": null\n" +
                "                    }\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"Post\",\n" +
                "                    \"path\": \"post\",\n" +
                "                    \"hidden\": false,\n" +
                "                    \"component\": \"system/post/index\",\n" +
                "                    \"meta\": {\n" +
                "                        \"title\": \"岗位管理\",\n" +
                "                        \"icon\": \"post\",\n" +
                "                        \"noCache\": false,\n" +
                "                        \"link\": null\n" +
                "                    }\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"Dict\",\n" +
                "                    \"path\": \"dict\",\n" +
                "                    \"hidden\": false,\n" +
                "                    \"component\": \"system/dict/index\",\n" +
                "                    \"meta\": {\n" +
                "                        \"title\": \"字典管理\",\n" +
                "                        \"icon\": \"dict\",\n" +
                "                        \"noCache\": false,\n" +
                "                        \"link\": null\n" +
                "                    }\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"Config\",\n" +
                "                    \"path\": \"config\",\n" +
                "                    \"hidden\": false,\n" +
                "                    \"component\": \"system/config/index\",\n" +
                "                    \"meta\": {\n" +
                "                        \"title\": \"参数设置\",\n" +
                "                        \"icon\": \"edit\",\n" +
                "                        \"noCache\": false,\n" +
                "                        \"link\": null\n" +
                "                    }\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"Match\",\n" +
                "                    \"path\": \"match\",\n" +
                "                    \"hidden\": false,\n" +
                "                    \"component\": \"system/match/index\",\n" +
                "                    \"meta\": {\n" +
                "                        \"title\": \"比赛信息\",\n" +
                "                        \"icon\": \"message\",\n" +
                "                        \"noCache\": false,\n" +
                "                        \"link\": null\n" +
                "                    }\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"Notice\",\n" +
                "                    \"path\": \"notice\",\n" +
                "                    \"hidden\": false,\n" +
                "                    \"component\": \"system/notice/index\",\n" +
                "                    \"meta\": {\n" +
                "                        \"title\": \"通知公告\",\n" +
                "                        \"icon\": \"message\",\n" +
                "                        \"noCache\": false,\n" +
                "                        \"link\": null\n" +
                "                    }\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"Log\",\n" +
                "                    \"path\": \"log\",\n" +
                "                    \"hidden\": false,\n" +
                "                    \"redirect\": \"noRedirect\",\n" +
                "                    \"component\": \"ParentView\",\n" +
                "                    \"alwaysShow\": true,\n" +
                "                    \"meta\": {\n" +
                "                        \"title\": \"日志管理\",\n" +
                "                        \"icon\": \"log\",\n" +
                "                        \"noCache\": false,\n" +
                "                        \"link\": null\n" +
                "                    },\n" +
                "                    \"children\": [\n" +
                "                        {\n" +
                "                            \"name\": \"Operlog\",\n" +
                "                            \"path\": \"operlog\",\n" +
                "                            \"hidden\": false,\n" +
                "                            \"component\": \"monitor/operlog/index\",\n" +
                "                            \"meta\": {\n" +
                "                                \"title\": \"操作日志\",\n" +
                "                                \"icon\": \"form\",\n" +
                "                                \"noCache\": false,\n" +
                "                                \"link\": null\n" +
                "                            }\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"name\": \"Logininfor\",\n" +
                "                            \"path\": \"logininfor\",\n" +
                "                            \"hidden\": false,\n" +
                "                            \"component\": \"monitor/logininfor/index\",\n" +
                "                            \"meta\": {\n" +
                "                                \"title\": \"登录日志\",\n" +
                "                                \"icon\": \"logininfor\",\n" +
                "                                \"noCache\": false,\n" +
                "                                \"link\": null\n" +
                "                            }\n" +
                "                        }\n" +
                "                    ]\n" +
                "                }\n" +
                "            ]\n" +
                "        },\n" +
                "        {\n" +
                "            \"name\": \"Monitor\",\n" +
                "            \"path\": \"/monitor\",\n" +
                "            \"hidden\": false,\n" +
                "            \"redirect\": \"noRedirect\",\n" +
                "            \"component\": \"Layout\",\n" +
                "            \"alwaysShow\": true,\n" +
                "            \"meta\": {\n" +
                "                \"title\": \"系统监控\",\n" +
                "                \"icon\": \"monitor\",\n" +
                "                \"noCache\": false,\n" +
                "                \"link\": null\n" +
                "            },\n" +
                "            \"children\": [\n" +
                "                {\n" +
                "                    \"name\": \"Online\",\n" +
                "                    \"path\": \"online\",\n" +
                "                    \"hidden\": false,\n" +
                "                    \"component\": \"monitor/online/index\",\n" +
                "                    \"meta\": {\n" +
                "                        \"title\": \"在线用户\",\n" +
                "                        \"icon\": \"online\",\n" +
                "                        \"noCache\": false,\n" +
                "                        \"link\": null\n" +
                "                    }\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"Job\",\n" +
                "                    \"path\": \"job\",\n" +
                "                    \"hidden\": false,\n" +
                "                    \"component\": \"monitor/job/index\",\n" +
                "                    \"meta\": {\n" +
                "                        \"title\": \"定时任务\",\n" +
                "                        \"icon\": \"job\",\n" +
                "                        \"noCache\": false,\n" +
                "                        \"link\": null\n" +
                "                    }\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"Druid\",\n" +
                "                    \"path\": \"druid\",\n" +
                "                    \"hidden\": false,\n" +
                "                    \"component\": \"monitor/druid/index\",\n" +
                "                    \"meta\": {\n" +
                "                        \"title\": \"数据监控\",\n" +
                "                        \"icon\": \"druid\",\n" +
                "                        \"noCache\": false,\n" +
                "                        \"link\": null\n" +
                "                    }\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"Server\",\n" +
                "                    \"path\": \"server\",\n" +
                "                    \"hidden\": false,\n" +
                "                    \"component\": \"monitor/server/index\",\n" +
                "                    \"meta\": {\n" +
                "                        \"title\": \"服务监控\",\n" +
                "                        \"icon\": \"server\",\n" +
                "                        \"noCache\": false,\n" +
                "                        \"link\": null\n" +
                "                    }\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"Cache\",\n" +
                "                    \"path\": \"cache\",\n" +
                "                    \"hidden\": false,\n" +
                "                    \"component\": \"monitor/cache/index\",\n" +
                "                    \"meta\": {\n" +
                "                        \"title\": \"缓存监控\",\n" +
                "                        \"icon\": \"redis\",\n" +
                "                        \"noCache\": false,\n" +
                "                        \"link\": null\n" +
                "                    }\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"CacheList\",\n" +
                "                    \"path\": \"cacheList\",\n" +
                "                    \"hidden\": false,\n" +
                "                    \"component\": \"monitor/cache/list\",\n" +
                "                    \"meta\": {\n" +
                "                        \"title\": \"缓存列表\",\n" +
                "                        \"icon\": \"redis-list\",\n" +
                "                        \"noCache\": false,\n" +
                "                        \"link\": null\n" +
                "                    }\n" +
                "                }\n" +
                "            ]\n" +
                "        },\n" +
                "        {\n" +
                "            \"name\": \"Tool\",\n" +
                "            \"path\": \"/tool\",\n" +
                "            \"hidden\": false,\n" +
                "            \"redirect\": \"noRedirect\",\n" +
                "            \"component\": \"Layout\",\n" +
                "            \"alwaysShow\": true,\n" +
                "            \"meta\": {\n" +
                "                \"title\": \"系统工具\",\n" +
                "                \"icon\": \"tool\",\n" +
                "                \"noCache\": false,\n" +
                "                \"link\": null\n" +
                "            },\n" +
                "            \"children\": [\n" +
                "                {\n" +
                "                    \"name\": \"Build\",\n" +
                "                    \"path\": \"build\",\n" +
                "                    \"hidden\": false,\n" +
                "                    \"component\": \"tool/build/index\",\n" +
                "                    \"meta\": {\n" +
                "                        \"title\": \"表单构建\",\n" +
                "                        \"icon\": \"build\",\n" +
                "                        \"noCache\": false,\n" +
                "                        \"link\": null\n" +
                "                    }\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"Gen\",\n" +
                "                    \"path\": \"gen\",\n" +
                "                    \"hidden\": false,\n" +
                "                    \"component\": \"tool/gen/index\",\n" +
                "                    \"meta\": {\n" +
                "                        \"title\": \"代码生成\",\n" +
                "                        \"icon\": \"code\",\n" +
                "                        \"noCache\": false,\n" +
                "                        \"link\": null\n" +
                "                    }\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"Swagger\",\n" +
                "                    \"path\": \"swagger\",\n" +
                "                    \"hidden\": false,\n" +
                "                    \"component\": \"tool/swagger/index\",\n" +
                "                    \"meta\": {\n" +
                "                        \"title\": \"系统接口\",\n" +
                "                        \"icon\": \"swagger\",\n" +
                "                        \"noCache\": false,\n" +
                "                        \"link\": null\n" +
                "                    }\n" +
                "                }\n" +
                "            ]\n" +
                "        },\n" +
                "        {\n" +
                "            \"name\": \"Http://ruoyi.vip\",\n" +
                "            \"path\": \"http://ruoyi.vip\",\n" +
                "            \"hidden\": false,\n" +
                "            \"component\": \"Layout\",\n" +
                "            \"meta\": {\n" +
                "                \"title\": \"若依官网\",\n" +
                "                \"icon\": \"guide\",\n" +
                "                \"noCache\": false,\n" +
                "                \"link\": \"http://ruoyi.vip\"\n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}";
    }

    @GetMapping("/getLoginUser")
    @ApiOperation(value = "获取当前登录用户信息", httpMethod = "GET")
    public Response<SysUser> getLoginUser() {
        return sysUserService.getLoginUser();
    }

    @PostMapping("/updatePassword")
    @ApiOperation(value = "修改密码", httpMethod = "POST")
    public Response updatePassword(@RequestBody @Valid UpdatePasswordVO vo) {
        return sysUserService.updatePassword(vo);
    }

    @GetMapping("/getGoogleToken")
    @ApiOperation(value = "获取谷歌令牌", httpMethod = "GET")
    Response<GetGoogleTokenVO> getGoogleToken() {
        return sysUserService.getGoogleToken();
    }

    @GetMapping("/bindGoogleToken")
    @ApiOperation(value = "绑定谷歌令牌", httpMethod = "GET")
    Response bindGoogleToken(@RequestParam("code") Long code, @RequestParam("secretKey") String secretKey) {
        return sysUserService.bindGoogleToken(code, secretKey);
    }

    @GetMapping("/unbindGoogleToken")
    @ApiOperation(value = "解绑谷歌令牌", httpMethod = "GET")
    Response unbindGoogleToken(String id) {
        return sysUserService.unbindGoogleToken(id);
    }

    @PostMapping("/insert")
    @ApiOperation(value = "插入后台用户", httpMethod = "POST")
    public Response<String> insert(@RequestBody @Valid SysUserInsertInput sysUserInsertInput) {
        String password = sysUserInsertInput.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        sysUserInsertInput.setPassword(password);
        sysUserService.insert(sysUserInsertInput);
        return Response.success("插入成功");
    }

    @PostMapping("/update")
    @ApiOperation(value = "更新后台用户", httpMethod = "POST")
    public Response<String> update(@RequestBody @Valid SysUserUpdateInput sysUserUpdateInput) {
        sysUserService.update(sysUserUpdateInput);
        return Response.success("修改成功");
    }

    @PostMapping("/delete")
    @ApiOperation(value = "删除后台用户", httpMethod = "POST")
    public Response<String> update(@RequestBody @Valid SysUserDeleteInput sysUserDeleteInput) {
        SysUser sysUser = sysUserService.queryById(sysUserDeleteInput.getId());
        if (sysUser.getUsername().equals("admin")) {
            throw new BizzRuntimeException(ResponseEnum.ADMIN_CANNOT_DELETE);
        }
        sysUserService.deleteById(sysUserDeleteInput.getId());
        return Response.success("删除成功");
    }

    @PostMapping("/list")
    @ApiOperation(value = "分页查询", httpMethod = "POST")
    public Response<PageInfo<SysUser>> list(@RequestBody @Valid ListSysUserInput listSysUserInput) {
        PageInfo<SysUser> pageInfo = sysUserService.list(listSysUserInput);
        return Response.success(pageInfo);
    }
}
