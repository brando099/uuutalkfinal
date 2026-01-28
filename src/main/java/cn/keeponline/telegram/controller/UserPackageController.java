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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
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
    public Response listByAccountIdAndStatus(Integer status, Integer pageNum, Integer pageSize) {
        String outId = sysUserContext.getAccountId();
        PageHelper.startPage(pageNum, pageSize);
        List<UserPackage> userPackages = userPackageMapper.listValidByAccountIdAndStatus(outId, status);
        return Response.success(new PageInfo<>(userPackages));
    }


    @RequestMapping("/addPackage")
    public Response addPackage(AddPackageInput addPackageInput)  {
        String outId = addPackageInput.getOutId();
        Integer packageCount = addPackageInput.getPackageCount();

        for (int i = 0; i < packageCount; i++) {
            UserPackage userPackage = new UserPackage();
            userPackage.setAccountId(outId);
            LocalDateTime now = LocalDateTime.now();
            if ("month".equals(addPackageInput.getDurationType())) {
                LocalDateTime localDateTime = now.plusMonths(1);
                Date expireTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                userPackage.setExpireTime(expireTime);
            } else if ("day".equals(addPackageInput.getDurationType())) {
                LocalDateTime localDateTime = now.plusDays(addPackageInput.getDuration());
                Date expireTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                userPackage.setExpireTime(expireTime);
            }

            userPackageMapper.insert(userPackage);
            log.info("套餐添加成功: {}", JSON.toJSONString(userPackage));
        }
        return Response.success("套餐添加成功");
    }

    @RequestMapping("/extendPackage")
    public Response extendPackage(@RequestBody ExtendPackageInput extendPackageInput) {
        Long packageId = extendPackageInput.getPackageId();
        Integer months = extendPackageInput.getMonths();
        UserPackage userPackage = userPackageMapper.selectById(packageId);

        if (userPackage == null) {
            throw new BizzRuntimeException("套餐id不存在");
        }

        Date expireTime = userPackage.getExpireTime();

        if (expireTime.compareTo(new Date()) < 0) {
            expireTime = new Date();
        }

        LocalDateTime expire = expireTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime localDateTime = expire.plusMonths(months);

        expireTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        userPackage.setExpireTime(expireTime);
        userPackageMapper.updateById(userPackage);
        log.info("套餐续期成功: {}", JSON.toJSONString(userPackage));
        return Response.success("套餐续期成功");
    }


}
