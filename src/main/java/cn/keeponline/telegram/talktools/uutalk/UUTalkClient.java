package cn.keeponline.telegram.talktools.uutalk;

import cn.keeponline.telegram.talktools.config.UUTalkGlobalConfig;
import cn.keeponline.telegram.talktools.core.*;
import cn.keeponline.telegram.talktools.handler.UUTalkOnMessage;
import cn.keeponline.telegram.talktools.logging.Logging;
import cn.keeponline.telegram.talktools.ws.ShareManager;
import cn.keeponline.telegram.talktools.ws.UUTalkWsCore;
import cn.keeponline.telegram.talktools.ws.WebSocketWrapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.slf4j.Logger;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static cn.keeponline.telegram.service.impl.TaskServiceImpl.uuuSocketMap;

/**
 * UUTalk WebSocket 客户端
 */
@Slf4j
public class UUTalkClient {
    private static final Logger logger = Logging.getLogger(UUTalkClient.class);
    private static final String WS_URL = "wss://ws.uuutalk.cc/";
    private static final UUTalkGlobalConfig GLOBAL_CONFIG = new UUTalkGlobalConfig();

    /**
     * 生成 DH 密钥对（X25519）
     */
    public static byte[][] generateDhKeypair() {
        SecureRandom random = new SecureRandom();
        X25519PrivateKeyParameters privateKey = new X25519PrivateKeyParameters(random);
        X25519PublicKeyParameters publicKey = privateKey.generatePublicKey();

        byte[] pubBytes = publicKey.getEncoded();
        byte[] privBytes = privateKey.getEncoded();

        return new byte[][]{pubBytes, privBytes};
    }

    /**
     * 构造 CONNECT 包
     */
    public static Map<String, Object> buildConnectPacket(String clientKeyB64, String uid, String token) {
        int protoVersion = 3;
        int deviceFlag = 1;
        String deviceId = UUID.randomUUID().toString().replace("-", "") + "W";

        Map<String, Object> packet = new HashMap<>();
        packet.put("version", protoVersion);
        packet.put("deviceFlag", deviceFlag);
        packet.put("deviceID", deviceId);
        packet.put("uid", uid);
        packet.put("token", token);
        packet.put("clientTimestamp", System.currentTimeMillis());
        packet.put("clientKey", clientKeyB64);
        packet.put("packetType", PacketType.CONNECT);

        logger.info("CONNECT 包内容: {}", packet);
        return packet;
    }

    /**
     * 启动 WebSocket 客户端
     */
    public static WebSocketWrapper runWsClient(String uid, String token) {
        WebSocketWrapper webSocketWrapper = uuuSocketMap.get(uid);
        if (webSocketWrapper != null) {
            return webSocketWrapper;
        }
        logger.info("run_ws_client 启动, uid={}, token={}", uid, token);

        // 更新全局参数
        GLOBAL_CONFIG.uid = uid;
        GLOBAL_CONFIG.token = token;

        // 创建 WebSocket
        Headers headers = new Headers.Builder()
                .add("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:146.0) Gecko/20100101 Firefox/146.0")
                .add("Accept", "*/*")
                .add("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
                .add("Origin", "https://web.uuutalk.co")
                .build();

        WebSocketWrapper ws = new WebSocketWrapper(WS_URL, "https://web.uuutalk.co", headers, uid);
        ws.setReconnectInterval(3);
        ws.setPingInterval(20);

        // onopen：连接成功 → 发 CONNECT 握手包
        ws.onOpen(() -> {
            logger.info("已连接服务器，准备发送 CONNECT 握手包...");

            try {
                // 1) 生成 DH 密钥对（X25519）
                byte[][] keypair = generateDhKeypair();
                byte[] pubBytes = keypair[0];
                byte[] privBytes = keypair[1];

                // 保存 DH 私钥
                ShareManager.DH_PRIVATE_KEY_BYTES = privBytes;
                logger.debug("DH 私钥 hex={}", bytesToHex(privBytes));

                // 2) 公钥 base64
                String clientKeyB64 = Base64.getEncoder().encodeToString(pubBytes);
                logger.debug("clientKey(base64)={}", clientKeyB64);

                // 3) 构造 CONNECT 包
                Map<String, Object> connectPacket = buildConnectPacket(clientKeyB64, uid, token);
                logger.info("CONNECT 包内容: {}", connectPacket);

                // 4) encode → send
                PacketEncoder encoder = new PacketEncoder();
                byte[] rawBytes = encoder.encode(connectPacket);
                logger.info("CONNECT 字节长度: {}", rawBytes.length);

                ws.send(rawBytes);
            } catch (Exception e) {
                logger.error("发送握手包失败", e);
            }
        });

        // 消息处理
        ws.onMessage(data -> UUTalkOnMessage.onPacket(data, ws));
        ws.onError(error -> logger.error("WS 出错", error));
        ws.onClose((code, reason) -> logger.info("WS 连接关闭: {} {}", code, reason));

        // 连接
        ws.connect();

        // 等待 AES ready
//        logger.info("等待 AES 准备好...");
//        while (!ShareManager.aesReady) {
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                return ws;
//            }
//        }

//        System.out.println(("AES 已准备好，可随时发送消息"));
//        boolean b = UUTalkWsCore.sendPictureMessage(ws, "file/preview/chat/2/d88d5141821740aeaa6366776f95dd50/1a3ba617f3384eb2978255a27ecdb7b7.png", "b6e2232835024a3bbf97742d44ee1f97", 2);
//        System.out.println("发送结果" + b);
        return ws;

    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

