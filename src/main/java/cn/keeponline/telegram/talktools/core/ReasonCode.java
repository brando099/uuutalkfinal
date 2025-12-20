package cn.keeponline.telegram.talktools.core;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务端返回的 reasonCode 枚举
 */
public enum ReasonCode {
    reasonUnknown(0, "未知原因"),
    reasonSuccess(1, "成功"),
    reasonAuthFail(2, "鉴权失败（token/uid 校验失败）"),
    reasonSubscriberNotExist(3, "订阅者不存在"),
    reasonInBlacklist(4, "在黑名单中，禁止操作"),
    reasonChannelNotExist(5, "频道不存在"),
    reasonUserNotOnNode(6, "用户不在当前节点"),
    reasonSenderOffline(7, "发送方已离线"),
    reasonMsgKeyError(8, "消息 msgKey 错误"),
    reasonPayloadDecodeError(9, "payload 解码失败"),
    reasonForwardSendPacketError(10, "转发发送消息失败"),
    reasonNotAllowSend(11, "不允许发送（权限或风控限制）"),
    reasonConnectKick(12, "连接被踢下线"),
    reasonNotInWhitelist(13, "不在白名单中"),
    reasonQueryTokenError(14, "查询 token 出错"),
    reasonSystemError(15, "系统内部错误");

    private final int value;
    private final String description;

    private static final Map<Integer, ReasonCode> BY_VALUE = new HashMap<>();

    static {
        for (ReasonCode code : values()) {
            BY_VALUE.put(code.value, code);
        }
    }

    ReasonCode(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static ReasonCode fromValue(int value) {
        return BY_VALUE.getOrDefault(value, reasonUnknown);
    }

    public static String getReasonDesc(int code) {
        ReasonCode reasonCode = fromValue(code);
        return code + "(" + reasonCode.description + ")";
    }
}

