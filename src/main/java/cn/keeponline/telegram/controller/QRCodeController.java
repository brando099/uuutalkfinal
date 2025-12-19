package cn.keeponline.telegram.controller;

import cn.keeponline.telegram.component.AsyncComponent;
import cn.keeponline.telegram.context.SysUserContext;
import cn.keeponline.telegram.dto.QRDTO;
import cn.keeponline.telegram.dto.YResponse;
import cn.keeponline.telegram.entity.UserInfo;
import cn.keeponline.telegram.entity.UserPackage;
import cn.keeponline.telegram.exception.BizzRuntimeException;
import cn.keeponline.telegram.mapper.UserInfoMapper;
import cn.keeponline.telegram.mapper.UserPackageMapper;
import cn.keeponline.telegram.response.Response;
import cn.keeponline.telegram.test.SendMessage;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class QRCodeController extends ControllerBase{
    @Autowired
    private AsyncComponent asyncComponent;

    @Autowired
    private SysUserContext sysUserContext;

    @Qualifier("userInfoMapper")
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserPackageMapper userPackageMapper;

    @RequestMapping("/getQRCode")
    public Response getQRCode(@RequestParam Long packageId) throws Exception {
        log.info("获取二维码入参: {}", packageId);
        String outId = sysUserContext.getAccountId();
        log.info("outId:{}", outId);
        UserPackage userPackage = userPackageMapper.getByIdAndAccountIdAndStatus(packageId, outId, 0);
        if (userPackage == null) {
            throw new BizzRuntimeException("套餐不存在或已被使用");
        }

        String qrcode = SendMessage.getQrcode();
        YResponse<QRDTO> qrDTO = JSON.parseObject(qrcode, new TypeReference<>() {
        });
        log.info("qrDTO:{}", JSON.toJSONString(qrDTO));
        QRDTO qrdto = qrDTO.getData();
        String confirm = qrdto.getConfirm();
        Long expire = qrdto.getExpire();
        String uuid = qrdto.getUuid();
        asyncComponent.checkQRCode(uuid, expire, outId, packageId);
        return Response.success(confirm);
    }

    @RequestMapping("/getStatus")
    public Response getStatus(String uuid) {
        UserInfo userInfo = userInfoMapper.getByUuid(uuid);
        if (userInfo != null) {
            return Response.success(true);
        } else {
            return Response.success(false);
        }
    }
}
