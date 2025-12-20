package cn.keeponline.telegram.talktools.core;

/**
 * 消息内容类型枚举
 */
public enum ContentType {
    TEXT(1),              // 普通文本
    IMAGE(2),             // 图片
    GIF(3),               // 动图
    STREAM(98),           // 流式消息（语音/视频）
    CMD(99),              // 指令类消息（系统消息、控制消息）
    SIGNAL_MESSAGE(21000); // 信令消息（RTC 用）

    private final int value;

    ContentType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

