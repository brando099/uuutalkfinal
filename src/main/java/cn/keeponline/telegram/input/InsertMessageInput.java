package cn.keeponline.telegram.input;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class InsertMessageInput {
    @NotNull(message = "用户id为空")
    private Long userId;
    @NotNull(message = "消息id为空")
    private Long messageId;
    @NotNull(message = "消息发送者id不能为空")
    private Long fromId;
    @NotNull(message = "消息接收者id为空")
    private Long peerId;
    @NotBlank(message = "消息内容不能为空")
    private String messageContent;
    @NotNull(message = "发送时间不能为空")
    private Long sendTime;
    @NotNull(message = "用户名")
    private String username;
}
