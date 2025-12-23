package cn.keeponline.telegram.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.keeponline.telegram.config.Constants;
import cn.keeponline.telegram.context.SysUserContext;
import cn.keeponline.telegram.entity.SysUser;
import cn.keeponline.telegram.entity.UserPackage;
import cn.keeponline.telegram.exception.BizzRuntimeException;
import cn.keeponline.telegram.input.ListSysUserInput;
import cn.keeponline.telegram.input.SysUserInsertInput;
import cn.keeponline.telegram.input.SysUserUpdateInput;
import cn.keeponline.telegram.mapper.SysUserMapper;
import cn.keeponline.telegram.mapper.UserPackageMapper;
import cn.keeponline.telegram.response.Response;
import cn.keeponline.telegram.response.ResponseEnum;
import cn.keeponline.telegram.service.SysUserService;
import cn.keeponline.telegram.utils.GoogleAuthenticator;
import cn.keeponline.telegram.utils.Json2;
import cn.keeponline.telegram.utils.Jwt2;
import cn.keeponline.telegram.vo.GetGoogleTokenVO;
import cn.keeponline.telegram.vo.SysUserLoginVO;
import cn.keeponline.telegram.vo.UpdatePasswordVO;
import com.alibaba.fastjson2.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 运营平台用户(SysUser)表服务实现类
 *
 * @author makejava
 * @since 2021-03-30 17:03:58
 */
@Service("sysUserService")
@Slf4j
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private Jwt2 jwt2;

    @Autowired
    private SysUserContext sysUserContext;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private UserPackageMapper userPackageMapper;


    @Override
    public SysUser getByOutId(String outId) {
        return sysUserMapper.getByOutId(outId);
    }

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public SysUser queryById(String id) {
        return this.sysUserMapper.queryById(id);
    }

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    @Override
    public List<SysUser> queryAllByLimit(int offset, int limit) {
        return this.sysUserMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param sysUserInsertInput 实例对象
     * @return 实例对象
     */
    @Override
    public SysUser insert(SysUserInsertInput sysUserInsertInput) {
        String username = sysUserInsertInput.getUsername();
        SysUser sysUserInDB = sysUserMapper.getByOutId(username);
        if (sysUserInDB != null) {
            throw new BizzRuntimeException("该账号已被占用");
        }

        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(sysUserInsertInput, sysUser);

        String password = DigestUtils.md5DigestAsHex((sysUserInsertInput.getPassword() + username).getBytes());
        sysUser.setOutId(username);
        sysUser.setPassword(password);
        sysUser.setStatus(0);
        sysUserMapper.insert(sysUser);

        Integer packageCount = sysUserInsertInput.getPackageCount();
        Integer validDays = sysUserInsertInput.getValidDays();

        // 添加套餐
        for (int i = 0; i < packageCount; i++) {
            UserPackage userPackage = new UserPackage();
            userPackage.setAccountId(username);
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime localDateTime = now.plusDays(validDays);
            Date expireTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            userPackage.setExpireTime(expireTime);
            userPackageMapper.insert(userPackage);
            log.info("套餐添加成功: {}", JSON.toJSONString(userPackage));
        }

        return sysUser;
    }

    /**
     * 修改数据
     *
     * @param sysUserUpdateInput 实例对象
     * @return 实例对象
     */
    @Override
    public void update(SysUserUpdateInput sysUserUpdateInput) {

        String id = sysUserUpdateInput.getId();

        SysUser sysUser = queryById(id);

        String password = DigestUtils.md5DigestAsHex((sysUserUpdateInput.getPassword() + sysUser.getUsername()).getBytes());
        sysUser.setStatus(sysUserUpdateInput.getStatus());

        if (StrUtil.isNotBlank(sysUserUpdateInput.getSecretKey())) {
            sysUser.setSecretKey(sysUserUpdateInput.getSecretKey());
        }

        if (sysUserUpdateInput.getGoogleAuthEnabled() != null) {
            sysUser.setGoogleAuthEnabled(sysUserUpdateInput.getGoogleAuthEnabled());
        }

        sysUser.setPassword(password);
        this.sysUserMapper.update(sysUser);
    }

    @Override
    public PageInfo<SysUser> list(ListSysUserInput listSysUserInput) {
        Integer pageNumber = listSysUserInput.getPageNumber();
        Integer pageSize = listSysUserInput.getPageSize();
        PageHelper.startPage(pageNumber, pageSize);
        List<SysUser> list = sysUserMapper.list(listSysUserInput);
        return new PageInfo<>(list);
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(String id) {
        return this.sysUserMapper.deleteById(id) > 0;
    }

    @Override
    public Response<String> login(SysUserLoginVO login) {
        SysUser user = this.verifyLogin(login);
        return Response.success(this.createTokenWithSave(user));
    }

    @Override
    public Response<SysUser> getLoginUser() {
        SysUser sysUser = this.sysUserMapper.queryById(this.sysUserContext.getRequestSysUserId());
        return Response.success(sysUser);
    }

    @Override
    public Response updatePassword(UpdatePasswordVO vo) {
        SysUser sysUser = this.sysUserMapper.queryById(this.sysUserContext.getRequestSysUserId());
        vo.setOldPassword(DigestUtils.md5DigestAsHex((vo.getOldPassword() + sysUser.getUsername()).getBytes()));
        if (!Objects.equals(sysUser.getPassword(), vo.getOldPassword())) {
            throw new BizzRuntimeException("原密码错误");
        }
        vo.setNewPassword(DigestUtils.md5DigestAsHex((vo.getNewPassword() + sysUser.getUsername()).getBytes()));
        sysUser.setPassword(vo.getNewPassword());
        this.sysUserMapper.update(sysUser);
        // 清理用户token 强制重新登录
//        this.redis2.delete(RedisKeys.USER_TOKEN + sysUser.getOutId());
        return Response.success();
    }

    @Override
    public Response<GetGoogleTokenVO> getGoogleToken() {
        SysUser user = this.sysUserMapper.queryById(this.sysUserContext.getRequestSysUserId());
        if (!Objects.isNull(user.getSecretKey()) && !Objects.equals(user.getSecretKey(), "")) {
            throw new BizzRuntimeException("已绑定Google令牌");
        }
        // 用户通过 谷歌app 绑定秘钥 然后通过app获取验证码 传给后台， 后台通过秘钥，校验验证码是否一致，一致通过
        String secretKey = GoogleAuthenticator.generateSecretKey();
        String qrCode = GoogleAuthenticator.getQRBarcodeURL(this.sysUserContext.getAccountId().toString(), "Telegram", secretKey);
        user.setSecretKey(secretKey);
        GetGoogleTokenVO getGoogleTokenVO = new GetGoogleTokenVO();
        getGoogleTokenVO.setQrCode(qrCode);
        getGoogleTokenVO.setSecretKey(secretKey);
        return Response.success(getGoogleTokenVO);
    }

    @Override
    public Response bindGoogleToken(Long code, String secretKey) {
        SysUser user = this.sysUserMapper.queryById(this.sysUserContext.getRequestSysUserId());
        GoogleAuthenticator googleAuth = new GoogleAuthenticator();
        boolean authResult = googleAuth.checkCode(secretKey, code, System.currentTimeMillis());
        if (!authResult) {
            throw new BizzRuntimeException("绑定失败, 验证码有误");
        }
        user.setSecretKey(secretKey);
        user.setGoogleAuthEnabled(true);
        this.sysUserMapper.update(user);
        return Response.success();
    }

    @Override
    public Response sendUnbindGoogleTokenMail() {
        return Response.success();
    }

    @Override
    public Response unbindGoogleToken(String id) {
        if (id == null) {
            SysUser user = this.sysUserContext.getRequestUser();
            id = user.getId();
        }

        this.sysUserMapper.cleanGoogleAuth(id);
        return Response.success();
    }

    private SysUser verifyLogin(SysUserLoginVO login) {
        String password = DigestUtils.md5DigestAsHex((login.getPassword() + login.getUsername()).getBytes());
        SysUser search = new SysUser();
        search.setUsername(login.getUsername());
        search.setPassword(password);

        search = this.sysUserMapper.queryOne(search);
        if (Objects.isNull(search)) {
            throw new BizzRuntimeException("账户或密码有误");
        }
        if (!Objects.isNull(search.getGoogleAuthEnabled())
                && !Objects.isNull(search.getSecretKey())
                && !Objects.equals(search.getSecretKey(), "")
                && search.getGoogleAuthEnabled()) {
            if (Objects.isNull(login.getGoogleToken())) {
                throw new BizzRuntimeException(ResponseEnum.RESULT_USER_REQUIRE_GOOGLE_AUTH.getMessage(),ResponseEnum.RESULT_USER_REQUIRE_GOOGLE_AUTH.getCode());
            }
            GoogleAuthenticator googleAuth = new GoogleAuthenticator();
            boolean authResult = googleAuth.checkCode(search.getSecretKey(), login.getGoogleToken(), System.currentTimeMillis());
            if (!authResult) {
                throw new BizzRuntimeException("Google令牌验证码不正确, 请重新输入");
            }
        }
        return search;
    }

    private String createTokenWithSave(SysUser user) {
        // jwt 数据
        Map<String, String> jwtData = Maps.newHashMap();
        jwtData.put(Constants.REQUEST_SYS_USER_ID, user.getId().toString());
        jwtData.put(Constants.REQUEST_OUT_ID, user.getOutId().toString());
        String token = this.jwt2.encode(jwtData);
        String data = Json2.toJson(user);
//        this.redis2.setString(RedisKeys.SYS_USER_TOKEN + user.getOutId(), data);
        return token;
    }

    public static void main(String[] args) {
        String path = System.getProperty("user.home") + "/yunipicture";
        System.out.println(path);
        File uploadDir = new File(path);
        if (!uploadDir.exists()) {
            boolean mkdirs = uploadDir.mkdirs();
            System.out.println(mkdirs);
        }
    }
}