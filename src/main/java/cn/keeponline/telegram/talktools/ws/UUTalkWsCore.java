package cn.keeponline.telegram.talktools.ws;

import cn.keeponline.telegram.talktools.cache.GlobalCache;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.keeponline.telegram.talktools.config.UUTalkGlobalConfig;
import cn.keeponline.telegram.talktools.core.PacketEncoder;
import cn.keeponline.telegram.talktools.core.PacketType;
import cn.keeponline.telegram.talktools.core.SendSetting;
import cn.keeponline.telegram.talktools.logging.Logging;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 等价 Python 的 uutalk_ws_core.py
 * 封装 SEND 文本消息的构造与发送逻辑。
 */
public final class UUTalkWsCore {

    private static final Logger logger = Logging.getLogger(UUTalkWsCore.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final UUTalkGlobalConfig GLOBAL_CONFIG = new UUTalkGlobalConfig();

    private UUTalkWsCore() {
    }

    /**
     * 构造并发送文本消息。
     * 等价 Python 的 send_text_message(...)
     */
    public static boolean sendTextMessage(
            WebSocketWrapper ws,
            String content,
            String channelId,
            int channelType,
            String uid
    ) {
        // 1. 构造消息体
        Map<String, Object> msg = new HashMap<>();
        msg.put("content", content);
        msg.put("type", 1);          // 文本
        msg.put("mention", new HashMap<>());  // 空对象
        msg.put("entities", java.util.List.of());

        // 2. 构造频道信息
        Map<String, Object> channel = new HashMap<>();
        channel.put("channelID", channelId);
        channel.put("channelType", channelType);

        // 3. 构造发送包
        Map<String, Object> packet = buildSendPacket(msg, channel, null);
        logger.info("发送消息体: {}", packet);

        // 4. 编码并发送（需要 AES key/iv 已经从 CONNACK 推导完成）
        ShareManager shareManager = GlobalCache.shareMap.get(uid);
        if (shareManager == null) {
            logger.warn("AES 尚未准备好，无法发送加密消息");
            return false;
        }

        PacketEncoder encoder = new PacketEncoder(shareManager.aesKey, shareManager.aesIv);
        byte[] raw = encoder.encode(packet);

        logger.info("发送 SEND 消息, 字节长度={}", raw.length);
        return ws.send(raw);
    }

    public static boolean sendPictureMessage(
            WebSocketWrapper ws,
            String url,
            String channelId,
            int channelType,
            String uid
    ) {
        // 1. 构造消息体
        Map<String, Object> msg = new HashMap<>();
        msg.put("width", 1440);
        msg.put("height", 1440);
        msg.put("url", url);
        msg.put("size", 74326);
        msg.put("isOriginalImage", true);
        msg.put("type", 2);

        // 2. 构造频道信息
        Map<String, Object> channel = new HashMap<>();
        channel.put("channelID", channelId);
        channel.put("channelType", channelType);

        // 3. 构造发送包
        Map<String, Object> packet = buildSendPacket(msg, channel, null);
        logger.info("发送消息体: {}", packet);

        // 4. 编码并发送（需要 AES key/iv 已经从 CONNACK 推导完成）
        ShareManager shareManager = GlobalCache.shareMap.get(uid);
        if (shareManager == null) {
            logger.warn("AES 尚未准备好，无法发送加密消息");
            return false;
        }

        PacketEncoder encoder = new PacketEncoder(shareManager.aesKey, shareManager.aesIv);
        byte[] raw = encoder.encode(packet);

        logger.info("发送 SEND 消息, 字节长度={}", raw.length);
        return ws.send(raw);
    }

    /**
     * 简化版发送函数，使用默认频道。
     * 等价 Python 的 send_text(...)
     */
    public static void sendText(
            WebSocketWrapper ws,
            String content
    ) {
        String channelId = GLOBAL_CONFIG.deviceId;
        int channelType = GLOBAL_CONFIG.defaultChannelType;
//        sendTextMessage(ws, content, channelId, channelType);
    }

    /**
     * 构造 SEND 协议包。
     * 等价 Python 的 build_send_packet(...)
     */
    public static Map<String, Object> buildSendPacket(
            Map<String, Object> msgBody,
            Map<String, Object> channel,
            Map<String, Object> options
    ) {
        // 1. 处理 options
        if (options == null) {
            options = new HashMap<>();
            options.put(
                    "setting",
                    new SendSetting(
                            true,   // receiptEnabled
                            false,  // topic
                            false,  // _streamOn
                            ""      // _streamNo
                    )
            );
            options.put("noPersist", false);
            options.put("reddot", true);
        }

        Object settingObj = options.get("setting");
        SendSetting setting;
        if (settingObj instanceof SendSetting) {
            setting = (SendSetting) settingObj;
        } else {
            // 兼容性兜底
            setting = new SendSetting(true, false, false, "");
        }

        // 2. payload：JSON -> UTF-8 bytes
        byte[] payloadBytes;
        try {
            payloadBytes = OBJECT_MAPPER.writeValueAsBytes(msgBody);
        } catch (Exception e) {
            logger.error("序列化消息体失败: {}", msgBody, e);
            // 兜底：发送空 JSON
            payloadBytes = "{}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
        logger.debug("[PAYLOAD] bytes={}", payloadBytes.length);

        // 3. 构造 SendPacket
        Map<String, Object> packet = new HashMap<>();
        packet.put("packetType", PacketType.SEND);   // SEND = 3
        packet.put("reddot", true);                  // JS 强制 reddot = true
        packet.put("noPersist", false);
        packet.put("setting", setting);
        packet.put("clientMsgNo", UUID.randomUUID().toString().replace("-", "") + "3");
        packet.put("streamNo", "");
        packet.put("clientSeq", ShareManager.nextClientSeq());
        packet.put("fromUID", GLOBAL_CONFIG.uid);
        packet.put("channelID", channel.get("channelID"));
        packet.put("channelType", channel.get("channelType"));
        packet.put("payload", payloadBytes);

        return packet;
    }
}


