package cn.keeponline.telegram.talktools.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据包解码器
 */
public class PacketDecoder {
    private static final Logger logger = LoggerFactory.getLogger(PacketDecoder.class);
    private int serverVersion = 3;

    public Map<String, Object> decode(byte[] data) {
        DecodeBuffer buf = new DecodeBuffer(data);

        Map<String, Object> framer = decodeFramer(buf);
        PacketType packetType = (PacketType) framer.get("packetType");

        if (packetType == PacketType.PING) {
            Map<String, Object> result = new HashMap<>();
            result.put("packetType", PacketType.PING);
            return result;
        }

        if (packetType == PacketType.PONG) {
            Map<String, Object> result = new HashMap<>();
            result.put("packetType", PacketType.PONG);
            return result;
        }

        return decodeBody(packetType, framer, buf);
    }

    private Map<String, Object> decodeFramer(DecodeBuffer buf) {
        int first = buf.readUint8();
        int packetTypeInt = first >> 4;
        int flags = first & 0x0F;

        PacketType packetType = PacketType.fromValue(packetTypeInt);

        Map<String, Object> framer = new HashMap<>();
        framer.put("packetType", packetType);
        framer.put("noPersist", (flags & 0b0001) > 0);
        framer.put("reddot", ((flags >> 1) & 0b0001) > 0);
        framer.put("syncOnce", ((flags >> 2) & 0b0001) > 0);
        framer.put("dup", ((flags >> 3) & 0b0001) > 0);

        if (packetType != PacketType.PING && packetType != PacketType.PONG) {
            framer.put("remainingLength", buf.readVariableLength());
        }

        // 特殊：CONNACK 使用最低位标记 hasServerVersion
        if (packetType == PacketType.CONNACK) {
            framer.put("hasServerVersion", (flags & 0b0001) > 0);
        }

        return framer;
    }

    private Map<String, Object> decodeBody(PacketType packetType, Map<String, Object> framer, DecodeBuffer buf) {
        switch (packetType) {
            case CONNACK:
                return decodeConnack(framer, buf);
            case DISCONNECT:
                return decodeDisconnect(framer, buf);
            case RECV:
                return decodeRecv(framer, buf);
            case SENDACK:
                return decodeSendack(framer, buf);
            case SUBACK:
                return decodeSuback(framer, buf);
            default:
                logger.warn("不支持的协议包 -> {}", packetType);
                return null;
        }
    }

    private Map<String, Object> decodeConnack(Map<String, Object> framer, DecodeBuffer buf) {
        Map<String, Object> result = new HashMap<>(framer);

        if (Boolean.TRUE.equals(framer.get("hasServerVersion"))) {
            int serverVersion = buf.readUint8();
            result.put("serverVersion", serverVersion);
            this.serverVersion = serverVersion;
            logger.info("服务器协议版本: {}", serverVersion);
        }

        result.put("timeDiff", buf.readInt64());
        result.put("reasonCode", buf.readUint8());
        result.put("serverKey", buf.readString());
        result.put("salt", buf.readString());

        int sv = (Integer) result.getOrDefault("serverVersion", 0);
        if (sv >= 4) {
            result.put("nodeId", buf.readInt64());
        }

        result.put("type", "CONNACK");
        return result;
    }

    private Map<String, Object> decodeRecv(Map<String, Object> framer, DecodeBuffer buf) {
        Map<String, Object> result = new HashMap<>(framer);

        int settingByte = buf.readUint8();
        RecvSetting setting = new RecvSetting(settingByte);
        result.put("setting", setting.toMap());

        result.put("msgKey", buf.readString());
        result.put("fromUID", buf.readString());
        result.put("channelID", buf.readString());
        result.put("channelType", buf.readUint8());

        if (serverVersion >= 3) {
            result.put("expire", buf.readInt32());
        }

        result.put("clientMsgNo", buf.readString());

        if (setting.isStreamOn()) {
            result.put("streamNo", buf.readString());
            result.put("streamSeq", buf.readInt32());
            result.put("streamFlag", buf.readUint8());
        }

        result.put("messageID", String.valueOf(buf.readInt64()));
        result.put("messageSeq", buf.readInt32());
        result.put("timestamp", buf.readInt32());

        if (setting.isTopic()) {
            result.put("topic", buf.readString());
        }

        result.put("payload", buf.readRemaining());
        result.put("type", "RECV");
        return result;
    }

    private Map<String, Object> decodeSendack(Map<String, Object> framer, DecodeBuffer buf) {
        Map<String, Object> result = new HashMap<>(framer);
        result.put("messageID", buf.readInt64());
        result.put("clientSeq", buf.readInt32());
        result.put("messageSeq", buf.readInt32());
        result.put("reasonCode", buf.readUint8());
        result.put("type", "SENDACK");
        return result;
    }

    private Map<String, Object> decodeDisconnect(Map<String, Object> framer, DecodeBuffer buf) {
        Map<String, Object> result = new HashMap<>(framer);
        result.put("reasonCode", buf.readUint8());
        result.put("reason", buf.readString());
        result.put("type", "DISCONNECT");
        return result;
    }

    private Map<String, Object> decodeSuback(Map<String, Object> framer, DecodeBuffer buf) {
        Map<String, Object> result = new HashMap<>(framer);
        result.put("type", "SUBACK");
        return result;
    }
}

