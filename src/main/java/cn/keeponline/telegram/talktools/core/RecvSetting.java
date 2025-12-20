package cn.keeponline.telegram.talktools.core;

import java.util.HashMap;
import java.util.Map;

/**
 * 接收消息的设置
 */
public class RecvSetting {
    private final int raw;
    private final boolean receiptEnabled;
    private final boolean topic;
    private final boolean streamOn;

    public RecvSetting(int value) {
        this.raw = value;
        this.receiptEnabled = ((value >> 7) & 1) > 0;
        this.topic = ((value >> 3) & 1) > 0;
        this.streamOn = ((value >> 2) & 1) > 0;
    }

    public int getRaw() {
        return raw;
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

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("raw", raw);
        map.put("receiptEnabled", receiptEnabled);
        map.put("topic", topic);
        map.put("streamOn", streamOn);
        return map;
    }
}

