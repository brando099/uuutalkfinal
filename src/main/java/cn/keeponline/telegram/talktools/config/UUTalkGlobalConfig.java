package cn.keeponline.telegram.talktools.config;

/**
 * 保存 WebSocket 公共参数：uid, token, deviceID, proto version...
 * 类似 JS 中 De.shared().config
 */
public class UUTalkGlobalConfig {
    public String uid = "";
    public String token = "";
    public String deviceId = "";
    public int protoVersion = 3;
    public int deviceFlag = 1;
    public String channelId = "b6e2232835024a3bbf97742d44ee1f97";
    public int defaultChannelType = 2; // 1 代表私聊 2 代表群发
    public String username = "";
    public String avatar = "";
    public String shortNo = "";
}

