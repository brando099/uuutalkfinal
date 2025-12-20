package cn.keeponline.telegram.talktools.core;

/**
 * 消息类型枚举，对应协议中的 packetType 值
 */
public enum PacketType {
    Reserved(0),    // 保留
    CONNECT(1),     // 客户端发起连接
    CONNACK(2),     // 服务端返回连接确认
    SEND(3),        // 客户端发送消息
    SENDACK(4),     // 服务端确认已收到消息
    RECV(5),        // 服务端推送消息给客户端
    RECVACK(6),     // 客户端确认已收到推送消息
    PING(7),        // 心跳请求
    PONG(8),        // 心跳响应
    DISCONNECT(9),  // 客户端断开连接
    SUB(10),        // 订阅主题
    SUBACK(11);     // 服务端返回订阅确认

    private final int value;

    PacketType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static PacketType fromValue(int value) {
        for (PacketType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown packet type: " + value);
    }
}

