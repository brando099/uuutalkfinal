package cn.keeponline.telegram.controller;

import cn.keeponline.telegram.context.SysUserContext;
import cn.keeponline.telegram.entity.UserPackage;
import cn.keeponline.telegram.exception.BizzRuntimeException;
import cn.keeponline.telegram.input.AddPackageInput;
import cn.keeponline.telegram.input.ExtendPackageInput;
import cn.keeponline.telegram.mapper.UserPackageMapper;
import cn.keeponline.telegram.response.Response;
import com.alibaba.fastjson2.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@RestController
@RequestMapping("/userPackage")
@Api(tags = "套餐管理")
@Slf4j
public class UserPackageController extends ControllerBase {

    @Autowired
    private SysUserContext sysUserContext;


    @Autowired
    private UserPackageMapper userPackageMapper;

    @RequestMapping("/getPackages")
    public Response getPackages() {
        String outId = sysUserContext.getAccountId();
        List<UserPackage> userPackages = userPackageMapper.listValidByAccountIdAndStatus(outId, 0);
        return Response.success(userPackages);
    }

    @RequestMapping("/listByAccountIdAndStatus")
    public Response listByAccountIdAndStatus(Integer status, Integer pageNum, Integer pageSize, String accountId) {
        String outId = sysUserContext.getAccountId();
        if (accountId != null && !accountId.trim().isEmpty()) {
            if (!"kes099".equals(outId)) {
                throw new BizzRuntimeException("没有权限查询其他账号套餐");
            }
            outId = accountId.trim();
        }
        PageHelper.startPage(pageNum, pageSize);
        List<UserPackage> userPackages = userPackageMapper.listValidByAccountIdAndStatus(outId, status);
        return Response.success(new PageInfo<>(userPackages));
    }


    @PostMapping("/addPackage")
    @Transactional(rollbackFor = Exception.class)
    public Response addPackage(@RequestBody AddPackageInput addPackageInput) {
        String accountId = sysUserContext.getAccountId();
        if (!"kes099".equals(accountId)) {
            throw new BizzRuntimeException("没有权限");
        }
        String outId = addPackageInput.getOutId();
        Integer packageCount = addPackageInput.getPackageCount();
        Integer duration = addPackageInput.getDuration();
        if (outId == null || outId.trim().isEmpty()) {
            throw new BizzRuntimeException("用户名不能为空");
        }
        if (packageCount == null || packageCount < 1 || duration == null || duration < 1) {
            throw new BizzRuntimeException("套餐数量和时长必须大于0");
        }
        if (!"month".equals(addPackageInput.getDurationType()) && !"day".equals(addPackageInput.getDurationType())) {
            throw new BizzRuntimeException("日期类型只支持month或day");
        }

        for (int i = 0; i < packageCount; i++) {
            UserPackage userPackage = new UserPackage();
            userPackage.setAccountId(outId.trim());
            userPackage.setPackageName(addPackageInput.getPackageName() == null || addPackageInput.getPackageName().trim().isEmpty()
                    ? "普通套餐" : addPackageInput.getPackageName().trim());
            LocalDateTime now = LocalDateTime.now();
            if ("month".equals(addPackageInput.getDurationType())) {
                LocalDateTime localDateTime = now.plusMonths(duration);
                Date expireTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                userPackage.setExpireTime(expireTime);
            } else if ("day".equals(addPackageInput.getDurationType())) {
                LocalDateTime localDateTime = now.plusDays(duration);
                Date expireTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                userPackage.setExpireTime(expireTime);
            }

            userPackageMapper.insert(userPackage);
            log.info("套餐添加成功: {}", JSON.toJSONString(userPackage));
        }
        return Response.success("套餐添加成功");
    }

    @PostMapping("/extendPackage")
    @Transactional(rollbackFor = Exception.class)
    public Response extendPackage(@RequestBody ExtendPackageInput extendPackageInput) {
        String accountId = sysUserContext.getAccountId();
        if (!"kes099".equals(accountId)) {
            throw new BizzRuntimeException("没有权限");
        }
        Integer months = extendPackageInput.getMonths();
        if (months == null || months < 1) {
            throw new BizzRuntimeException("续期月数必须大于0");
        }
        List<Long> packageIds = extendPackageInput.getPackageIds() == null
                ? new ArrayList<>() : new ArrayList<>(new LinkedHashSet<>(extendPackageInput.getPackageIds()));
        if (packageIds.isEmpty()) {
            throw new BizzRuntimeException("请选择要续期的套餐");
        }
        for (Long packageId : packageIds) {
            UserPackage userPackage = userPackageMapper.selectById(packageId);
            if (userPackage == null) {
                throw new BizzRuntimeException("套餐id不存在:" + packageId);
            }
            Date expireTime = userPackage.getExpireTime();
            if (expireTime == null || expireTime.compareTo(new Date()) < 0) {
                expireTime = new Date();
            }
            LocalDateTime expire = expireTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            expireTime = Date.from(expire.plusMonths(months).atZone(ZoneId.systemDefault()).toInstant());
            userPackage.setExpireTime(expireTime);
            if (Integer.valueOf(2).equals(userPackage.getStatus())) {
                userPackage.setStatus(userPackage.getUid() == null || userPackage.getUid().trim().isEmpty() ? 0 : 1);
            }
            userPackageMapper.updateById(userPackage);
            log.info("套餐续期成功: {}", JSON.toJSONString(userPackage));
        }
        return Response.success("套餐续期成功");
    }

    @GetMapping("/deletePackage")
    public Response deletePackage(Long id) {
        if (!"kes099".equals(sysUserContext.getAccountId())) {
            throw new BizzRuntimeException("没有权限");
        }
        UserPackage userPackage = userPackageMapper.selectById(id);
        if (userPackage == null) {
            throw new BizzRuntimeException("套餐不存在");
        }
        userPackageMapper.deleteById(id);
        return Response.success("删除套餐成功");
    }


}
