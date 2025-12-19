package cn.keeponline.telegram.service;

import cn.keeponline.telegram.entity.SysUser;
import cn.keeponline.telegram.input.ListSysUserInput;
import cn.keeponline.telegram.input.SysUserInsertInput;
import cn.keeponline.telegram.input.SysUserUpdateInput;
import cn.keeponline.telegram.response.Response;
import cn.keeponline.telegram.vo.GetGoogleTokenVO;
import cn.keeponline.telegram.vo.SysUserLoginVO;
import cn.keeponline.telegram.vo.UpdatePasswordVO;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 运营平台用户(SysUser)表服务接口
 *
 * @author makejava
 * @since 2021-03-30 17:03:58
 */
public interface SysUserService {

    SysUser getByOutId(String outId);

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    SysUser queryById(String id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<SysUser> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param sysUserInsertInput 实例对象
     * @return 实例对象
     */
    SysUser insert(SysUserInsertInput sysUserInsertInput);

    /**
     * 修改数据
     */
    void update(SysUserUpdateInput sysUserUpdateInput);

    /**
     * 查询列表
     *
     * @param listSysUserInput 查询对象
     * @return 返回信息
     */
    PageInfo<SysUser> list(ListSysUserInput listSysUserInput);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(String id);

    /**
     * 登录
     *
     * @param login 登录对象
     * @return 是否成功
     */
    Response<String> login(SysUserLoginVO login);

    /**
     * 获取当前登录用户信息
     *
     * @return 是否成功
     */
    Response<SysUser> getLoginUser();

    /**
     * 修改密码
     *
     * @return 是否成功
     */
    Response updatePassword(UpdatePasswordVO vo);

    /**
     * 获取谷歌令牌
     *
     * @return 令牌二维码
     */
    Response<GetGoogleTokenVO> getGoogleToken();

    /**
     * 绑定谷歌令牌
     *
     * @return 是否成功
     */
    Response bindGoogleToken(Long code, String secretKey);

    /**
     * 解绑发送谷歌令牌邮箱验证码
     *
     * @return 是否成功
     */
    Response sendUnbindGoogleTokenMail();

    /**
     * 解绑谷歌令牌
     *
     * @return 是否成功
     */
    Response unbindGoogleToken(String id);

}