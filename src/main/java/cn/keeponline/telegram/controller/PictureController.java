package cn.keeponline.telegram.controller;

import cn.keeponline.telegram.component.AsyncComponent;
import cn.keeponline.telegram.context.SysUserContext;
import cn.keeponline.telegram.dto.QRDTO;
import cn.keeponline.telegram.dto.YResponse;
import cn.keeponline.telegram.response.Response;
import cn.keeponline.telegram.test.SendMessage;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForArraysOfBoolean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


@RestController
@Slf4j
@RequestMapping("/picture")
public class PictureController {
    @Autowired
    private AsyncComponent asyncComponent;

    @Autowired
    private SysUserContext sysUserContext;


    @PostMapping("/upload")
    public Response getQRCode(MultipartFile file) throws Exception {
        String uid = "964481395340619776";
        String token = "c8906e5c35262d607cc303d6eebc474e";
        long size = file.getSize();
        String name = file.getName();
        log.info("size: {}, name: {}", size, name);
//        JSONObject jsonPicture = SendMessage.getPictureRoute(uid, token, file);
//        String md5 = DigestUtils.md5DigestAsHex(file.getInputStream());
//
//        File tempFile = File.createTempFile("upload-", "-" + file.getOriginalFilename());
//        file.transferTo(tempFile);
//
//        cn.hutool.http.HttpResponse qiResponse = cn.hutool.http.HttpRequest.post("http://localhost:8080/system/match/picture")
//                .header("User-Agent", "UneedGroup/3.8.0 BROWSER/11.5.0 (darwin-x64;Release;Build-0-9-0)")
//                .header("Content-Type", "multipart/form-data")
//                .form("file", tempFile)
//                .header("Accept", "*/*")
//                .charset(StandardCharsets.UTF_8)
//                .timeout(120000)
//                .execute();
//        long size1 = file.getSize();
//        log.info("size1: {}", size1);
//
//        log.info("md5: {}", md5);
        return Response.success();
    }

}
