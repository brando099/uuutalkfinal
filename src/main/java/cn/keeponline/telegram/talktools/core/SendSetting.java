package cn.keeponline.telegram.talktools.core;

/**
 * 发送消息的设置
 */
public class SendSetting {
    private final boolean receiptEnabled;
    private final boolean topic;
    private final boolean streamOn;
    private final String streamNo;

    public SendSetting(boolean receiptEnabled, boolean topic, boolean streamOn, String streamNo) {
        this.receiptEnabled = receiptEnabled;
        this.topic = topic;
        this.streamOn = streamOn;
        this.streamNo = streamNo != null ? streamNo : "";
    }

    public boolean isReceiptEnabled() {
        return receiptEnabled;
    }

    public boolean isTopic() {
        return topic;
    }

    public boolean isStreamOn() {
        return streamOn;
    }

    public String getStreamNo() {
        return streamNo;
    }

    /**
     * 将设置转换为 uint8 值（位标志）
     */
    public int toUint8() {
        int val = 0;
        if (receiptEnabled) {
            val |= 1 << 7;  // 128
        }
        // 其他位根据实际协议定义
        return val;
    }
}

