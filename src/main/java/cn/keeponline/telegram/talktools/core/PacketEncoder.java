package cn.keeponline.telegram.talktools.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

/**
 * 数据包编码器
 */
public class PacketEncoder {
    private static final Logger logger = LoggerFactory.getLogger(PacketEncoder.class);
    private final AESCipher crypto;

    public PacketEncoder() {
        this.crypto = null;
    }

    public PacketEncoder(String aesKey, String aesIv) {
        this.crypto = new AESCipher(aesKey, aesIv);
    }

    /**
     * 编码数据包
     */
    public byte[] encode(Map<String, Object> packet) {
        PacketByteBuffer buf = new PacketByteBuffer();
        PacketType packetType = (PacketType) packet.get("packetType");

        // PING / PONG 没有消息体
        if (packetType == PacketType.PING || packetType == PacketType.PONG) {
            byte[] header = encodeFramer(packet, 0);
            buf.writeBytes(header);
        } else {
            // 1. 编码消息体（payload）
            byte[] body = encodeBody(packetType, packet);
            logger.debug("encoded body length={}", body.length);

            // 2. 编码协议头（framer），长度 = body 长度
            byte[] header = encodeFramer(packet, body.length);

            // 3. 组合：协议头 + 消息体
            buf.writeBytes(header);
            buf.writeBytes(body);
        }

        return buf.toBytes();
    }

    /**
     * 编码协议头
     */
    private byte[] encodeFramer(Map<String, Object> packet, int bodyLength) {
        PacketType packetType = (PacketType) packet.get("packetType");

        // PING/PONG 特例
        if (packetType == PacketType.PING || packetType == PacketType.PONG) {
            return new byte[]{(byte) (packetType.getValue() << 4)};
        }

        // 标志位
        boolean dup = (Boolean) packet.getOrDefault("dup", false);
        boolean syncOnce = (Boolean) packet.getOrDefault("syncOnce", false);
        boolean reddot = (Boolean) packet.getOrDefault("reddot", false);
        boolean noPersist = (Boolean) packet.getOrDefault("noPersist", false);

        int flags = (encodeBool(dup) << 3)
                | (encodeBool(syncOnce) << 2)
                | (encodeBool(reddot) << 1)
                | encodeBool(noPersist);

        PacketByteBuffer header = new PacketByteBuffer();
        // 第一个字节：高 4 位是 packetType，低 4 位是 flags
        header.writeUint8((packetType.getValue() << 4) | flags);

        // 可变长度编码 body_length
        byte[] lengthBytes = encodeVariableLength(bodyLength);
        header.writeBytes(lengthBytes);

        return header.toBytes();
    }

    private int encodeBool(boolean value) {
        return value ? 1 : 0;
    }

    private byte[] encodeVariableLength(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("value must be non-negative");
        }

        PacketByteBuffer out = new PacketByteBuffer();
        while (true) {
            int digit = value & 0x7F;
            value >>= 7;
            if (value > 0) {
                digit |= 0x80;
            }
            out.writeUint8(digit);
            if (value == 0) {
                break;
            }
        }
        return out.toBytes();
    }

    private byte[] encodeBody(PacketType packetType, Map<String, Object> packet) {
        switch (packetType) {
            case CONNECT:
                return encodeConnect(packet);
            case SEND:
                return encodeSend(packet);
            case RECVACK:
                return encodeRecvack(packet);
            default:
                throw new UnsupportedOperationException("Unsupported packet type: " + packetType);
        }
    }

    private byte[] encodeConnect(Map<String, Object> packet) {
        PacketByteBuffer buf = new PacketByteBuffer();

        buf.writeUint8((Integer) packet.get("version"));
        buf.writeUint8((Integer) packet.get("deviceFlag"));
        buf.writeString((String) packet.get("deviceID"));
        buf.writeString((String) packet.get("uid"));
        buf.writeString((String) packet.get("token"));
        buf.writeInt64((Long) packet.get("clientTimestamp"));
        buf.writeString((String) packet.get("clientKey"));

        return buf.toBytes();
    }

    private byte[] encodeSend(Map<String, Object> packet) {
        if (crypto == null) {
            throw new IllegalStateException("PacketEncoder.crypto 未初始化，请在构造时传入 aes_key / aes_iv");
        }

        PacketByteBuffer buf = new PacketByteBuffer();

        SendSetting setting = (SendSetting) packet.get("setting");
        if (setting == null) {
            throw new IllegalArgumentException("packet['setting'] is required for SEND packet");
        }

        buf.writeUint8(setting.toUint8());

        int clientSeq = (Integer) packet.getOrDefault("clientSeq", 0);
        buf.writeInt32(clientSeq);

        String clientMsgNo = (String) packet.get("clientMsgNo");
        if (clientMsgNo == null || clientMsgNo.isEmpty()) {
            clientMsgNo = generateClientMsgNo();
            packet.put("clientMsgNo", clientMsgNo);
        }
        buf.writeString(clientMsgNo);

        if (setting.isStreamOn()) {
            String streamNo = (String) packet.getOrDefault("streamNo", "");
            buf.writeString(streamNo);
        }

        String channelID = (String) packet.getOrDefault("channelID", "");
        int channelType = (Integer) packet.getOrDefault("channelType", 0);
        buf.writeString(channelID);
        buf.writeUint8(channelType);

        // expire (协议版本 >= 3)
        Integer expire = (Integer) packet.get("expire");
        if (expire != null) {
            buf.writeInt32(expire);
        } else {
            buf.writeInt32(0);
        }

        // === 加密处理区域 ===
        byte[] rawPayload = getPayloadBytes(packet.get("payload"));
        String nStr = crypto.encryption2(rawPayload);
        byte[] nBytes = nStr.getBytes(StandardCharsets.UTF_8);

        // veritifyString
        String verifyStr = clientSeq + clientMsgNo + channelID + channelType + nStr;
        String r = crypto.encryption(verifyStr);

        // MD5
        String ticket = md5(r);
        buf.writeString(ticket);

        // topic
        if (setting.isTopic()) {
            String topic = (String) packet.getOrDefault("topic", "");
            buf.writeString(topic);
        }

        // 写入 n 本体
        buf.writeBytes(nBytes);

        return buf.toBytes();
    }

    private byte[] encodeRecvack(Map<String, Object> packet) {
        PacketByteBuffer buf = new PacketByteBuffer();
        buf.writeInt64(((Number) packet.get("messageID")).longValue());
        buf.writeInt32((Integer) packet.get("messageSeq"));
        return buf.toBytes();
    }

    private byte[] getPayloadBytes(Object payload) {
        if (payload instanceof byte[]) {
            return (byte[]) payload;
        } else if (payload instanceof String) {
            return ((String) payload).getBytes(StandardCharsets.UTF_8);
        } else {
            throw new IllegalArgumentException("Unsupported payload type: " + payload.getClass());
        }
    }

    private String generateClientMsgNo() {
        // 生成类似 "xxxxxxxxxxxx4xxxyxxxxxxxxxxxxxxx" 的随机字符串
        String template = "xxxxxxxxxxxx4xxxyxxxxxxxxxxxxxxx";
        StringBuilder sb = new StringBuilder();
        for (char c : template.toCharArray()) {
            if (c == 'x' || c == 'y') {
                int t = (int) (Math.random() * 16);
                if (c == 'x') {
                    sb.append(Integer.toHexString(t));
                } else {
                    sb.append(Integer.toHexString((t & 3) | 8));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 failed", e);
        }
    }
}

