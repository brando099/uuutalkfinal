package cn.keeponline.telegram.controller;

import cn.hutool.core.util.StrUtil;
import cn.keeponline.telegram.dto.MessageDetailPageDTO;
import cn.keeponline.telegram.entity.Message;
import cn.keeponline.telegram.entity.User;
import cn.keeponline.telegram.input.ListMessageByUserIdInput;
import cn.keeponline.telegram.input.InsertMessageInput;
import cn.keeponline.telegram.input.ListMessageDetailInput;
import cn.keeponline.telegram.mapper.MessageMapper;
import cn.keeponline.telegram.mapper.UserMapper;
import cn.keeponline.telegram.response.Response;
import cn.keeponline.telegram.service.MessageService;
import cn.keeponline.telegram.vo.MessageVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/message")
@Api(tags = "消息管理")
public class MessageController extends ControllerBase {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageService messageService;

    @PostMapping("/insert")
    @ApiOperation("新增消息")
    public Response insert(@Valid @RequestBody List<InsertMessageInput> insertMessageInput) {

        for (InsertMessageInput messageInput : insertMessageInput) {
            String messageContent = messageInput.getMessageContent();
            if (StrUtil.isBlank(messageContent)) {
                continue;
            }
            try {
                Long userId = messageInput.getUserId();
                String username = messageInput.getUsername();
                if (username == null) {
                    username = "";
                }
                User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserId, userId));
                if (user == null) {
                    user = new User();
                    user.setUserId(userId);
                    user.setUsername(username);
                    user.setCreateTime(new Date());
                    userMapper.insert(user);
                } else {
                    if (!username.equals(user.getUsername())) {
                        user.setUsername(username);
                        userMapper.updateById(user);
                    }
                }

                Long otherId = userId;
                Long fromId = messageInput.getFromId();
                Long peerId = messageInput.getPeerId();

                if (!userId.equals(fromId)) {
                    otherId = fromId;
                }

                if (!userId.equals(peerId)) {
                    otherId = peerId;
                }

                Message message = new Message();
                BeanUtils.copyProperties(messageInput, message);

                message.setOtherId(otherId);
                message.setSendTime(new Date(messageInput.getSendTime() * 1000));
                message.setCreateTime(new Date());
                messageMapper.insert(message);
            } catch (Exception e) {

            }
        }
        return Response.success("请求成功");
    }

    @ApiOperation("获取用户聊天好友列表")
    @PostMapping("/listMessageByUserId")
    public Response<MessageVO> listMessageByUserId(@Valid @RequestBody ListMessageByUserIdInput listMessageByUserIdInput) {
        Long userId = listMessageByUserIdInput.getUserId();
        Integer pageNumber = listMessageByUserIdInput.getPageNumber();
        Integer pageSize = listMessageByUserIdInput.getPageSize();
        MessageVO messageVO = messageService.getMessageList(userId, pageNumber, pageSize);
        return Response.success(messageVO);
    }

    @ApiOperation("获取用户聊天详情页")
    @PostMapping("/listMessageDetail")
    public Response<PageInfo<MessageDetailPageDTO>> listMessageDetail(@Valid @RequestBody ListMessageDetailInput listMessageDetailInput) {
        Long userId = listMessageDetailInput.getUserId();
        Long otherId = listMessageDetailInput.getOtherId();
        Integer pageNumber = listMessageDetailInput.getPageNumber();
        Integer pageSize = listMessageDetailInput.getPageSize();
        PageInfo<MessageDetailPageDTO> pageInfo = messageService.listMessageDetail(userId, otherId, pageNumber, pageSize);
        return Response.success(pageInfo);
    }
}
