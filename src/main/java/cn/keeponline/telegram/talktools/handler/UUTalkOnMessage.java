package cn.keeponline.telegram.talktools.handler;

import cn.keeponline.telegram.talktools.cache.GlobalCache;
import cn.keeponline.telegram.talktools.core.*;
import cn.keeponline.telegram.talktools.logging.Logging;
import cn.keeponline.telegram.talktools.ws.ShareManager;
import cn.keeponline.telegram.talktools.ws.WebSocketWrapper;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * WebSocket 消息处理器
 */
public class UUTalkOnMessage {
    private static final Logger logger = Logging.getLogger(UUTalkOnMessage.class);
    private static final PacketDecoder decoder = new PacketDecoder();


    public static void onPacket(byte[] rawBytes, WebSocketWrapper ws) {
        try {
            Map<String, Object> packet = decoder.decode(rawBytes);
//            logger.debug("收到包内容: {}", packet);
        } catch (Exception e) {
            logger.error("解码失败", e);
            return;
        }

        Map<String, Object> packet = decoder.decode(rawBytes);
        PacketType packetType = (PacketType) packet.get("packetType");
//        logger.info("收到包类型: {}", packetType);

        // CONNACK 处理
        if (packetType == PacketType.CONNACK) {
            handleConnack(packet, ws);
            return;
        }

        // PONG 处理
        if (packetType == PacketType.PONG) {
            long now = System.currentTimeMillis();
            long delay = ShareManager.pingTime > 0 ? now - ShareManager.pingTime : -1;
//            logger.info("收到 PONG，网络延迟约 {} ms", delay);
            ShareManager.pingRetryCount = 0;
            return;
        }

        // DISCONNECT 处理
        if (packetType == PacketType.DISCONNECT) {
//            logger.warn("连接被踢");
            return;
        }

        // 其他消息处理
        onPacketHandler(packet, ws);
    }

    private static void handleConnack(Map<String, Object> packet, WebSocketWrapper ws) {
//        logger.info("CONNACK 内容:");
        String nodeId = String.valueOf(packet.getOrDefault("nodeId", ""));
        Integer reasonCode = (Integer) packet.get("reasonCode");
//        logger.info("成功连接到节点[{}], reasonCode={}", nodeId, reasonCode);

        String uid = ws.getUid();
        if (reasonCode != null && reasonCode == 1) {
            try {
                ShareManager shareManager = GlobalCache.shareMap.get(uid);
                UUTalkTool tool = new UUTalkTool(shareManager.DH_PRIVATE_KEY_BYTES);
                String[] aes = tool.deriveAesFromConnack(packet, shareManager.DH_PRIVATE_KEY_BYTES);
                String aesKey = aes[0];
                String aesIv = aes[1];
                boolean aesReady = true;
//                ShareManager shareManager = new ShareManager();
                shareManager.setAesKey(aesKey);
                shareManager.setAesIv(aesIv);
                shareManager.setAesReady(aesReady);

                GlobalCache.shareMap.put(uid, shareManager);

//                logger.info("[CONNACK] AES 解密环境准备完成");
            } catch (Exception e) {
                logger.error("推导 AES Key/IV 失败", e);
            }
        } else {
            logger.warn("[CONNACK] 连接失败，reasonCode={}", reasonCode);
        }
    }

    private static void onPacketHandler(Map<String, Object> packet, WebSocketWrapper ws) {
//        logger.info("收到消息包...: {}", packet);

        if (packet.get("packetType") == PacketType.SENDACK) {
            return;
        }
        String uid = ws.getUid();
        ShareManager shareManager = GlobalCache.shareMap.get(uid);
        if (shareManager == null) {
            logger.warn("AES 尚未准备好，无法发送加密消息");
            return;
        }


        byte[] payload = (byte[]) packet.get("payload");
        if (payload == null) {
            return;
        }

        try {
            byte[] plain = UUTalkTool.decryptPayloadBase64(shareManager.aesKey, shareManager.aesIv, payload);
            String text = new String(plain, StandardCharsets.UTF_8);
//            logger.info("解密文本: {}", text);
        } catch (Exception e) {
            logger.error("解密失败", e);
        }
    }
}

