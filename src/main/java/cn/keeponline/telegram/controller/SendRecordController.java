package cn.keeponline.telegram.controller;

import cn.keeponline.telegram.entity.SendRecord;
import cn.keeponline.telegram.mapper.SendRecordMapper;
import cn.keeponline.telegram.response.Response;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sendRecord")
@Api(tags = "会员管理")
@Slf4j
public class SendRecordController extends ControllerBase {

    @Autowired
    private SendRecordMapper sendMessageMapper;

    @RequestMapping("/listByUid")
    public Response listByAccountId(String uid) {
        List<SendRecord> sendRecords = sendMessageMapper.listByUid(uid);
        return Response.success(sendRecords);
    }
}
