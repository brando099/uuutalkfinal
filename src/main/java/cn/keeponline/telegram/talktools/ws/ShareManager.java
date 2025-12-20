package cn.keeponline.telegram.talktools.ws;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocket 共享管理器
 * 存储会话级状态
 */
public class ShareManager {
    public static final int PROTO_VERSION = 3;
    public static final String CLIENT_VERSION = "1.0.0";

    // DH 私钥
    public static byte[] DH_PRIVATE_KEY_BYTES = new byte[32]; // 需要初始化

    // 会话级 AES 状态
    public static String aesKey = null;
    public static String aesIv = null;
    public static boolean aesReady = false;

    // WS 心跳配置
    public static long pingTime = 0;
    public static int pingRetryCount = 0;
    public static int pingMaxRetryCount = 3;

    // clientSeq 递增计数
    private static final AtomicInteger clientSeq = new AtomicInteger(0);

    public static int nextClientSeq() {
        return clientSeq.incrementAndGet();
    }
}

