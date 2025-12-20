package cn.keeponline.telegram.talktools.core;

import org.bouncycastle.crypto.agreement.X25519Agreement;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * 工具集合：
 * - MD5 计算
 * - X25519 ECDH
 * - 从 CONNACK 推导 AES key / IV
 * - 解密 payload
 */
public class UUTalkTool {
    private final byte[] dhPrivateKey;

    public UUTalkTool(byte[] dhPrivateKey) {
        this.dhPrivateKey = dhPrivateKey;
    }

    /**
     * MD5 计算
     */
    public static String md5String(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 failed", e);
        }
    }

    /**
     * X25519 ECDH 密钥交换
     */
    public static byte[] ecdhX25519(byte[] secretKeyBytes, byte[] peerPublicBytes) {
        if (secretKeyBytes.length != 32) {
            throw new IllegalArgumentException("wrong secret key length");
        }
        if (peerPublicBytes.length != 32) {
            throw new IllegalArgumentException("wrong public key length");
        }

        try {
            X25519PrivateKeyParameters privateKey = new X25519PrivateKeyParameters(secretKeyBytes, 0);
            X25519PublicKeyParameters publicKey = new X25519PublicKeyParameters(peerPublicBytes, 0);

            X25519Agreement agreement = new X25519Agreement();
            agreement.init(privateKey);
            byte[] shared = new byte[32];
            agreement.calculateAgreement(publicKey, shared, 0);
            return shared;
        } catch (Exception e) {
            throw new RuntimeException("ECDH failed", e);
        }
    }

    /**
     * 从 CONNACK 中推导 AES Key / IV
     */
    public String[] deriveAesFromConnack(java.util.Map<String, Object> packet, byte[] dhPrivateKey) {
        String serverKeyB64 = (String) packet.get("serverKey");
        String salt = (String) packet.getOrDefault("salt", "");

        if (serverKeyB64 == null || serverKeyB64.isEmpty()) {
            throw new IllegalArgumentException("CONNACK 缺少 serverKey");
        }

        byte[] keyToUse = dhPrivateKey != null ? dhPrivateKey : this.dhPrivateKey;
        if (keyToUse == null) {
            throw new IllegalArgumentException("dh_private_key 未提供且实例中也没有配置");
        }

        // 1) 服务端公钥：base64 → bytes(32)
        byte[] serverPubBytes = Base64.getDecoder().decode(serverKeyB64);

        // 2) ECDH 共享密钥
        byte[] sharedKey = ecdhX25519(keyToUse, serverPubBytes);

        // 3) 共享密钥 → base64 字符串
        String s = Base64.getEncoder().encodeToString(sharedKey);

        // 4) MD5(s) → 32 位 hex，小写
        String c = md5String(s);

        // 5) 前 16 个字符做 AES Key
        String aesKey = c.substring(0, 16);

        // 6) salt 前 16 字符做 IV（或整个 salt）
        String aesIv = salt.length() > 16 ? salt.substring(0, 16) : salt;

        return new String[]{aesKey, aesIv};
    }

    /**
     * 解密 payload
     */
    public static byte[] decryptPayloadBase64(String aesKey, String aesIv, byte[] payloadBytes) {
        try {
            // 1) Uint8Array → 字符串（Base64 文本）
            String b64Text = new String(payloadBytes, StandardCharsets.UTF_8);

            // 2) Base64 → 真正密文字节
            byte[] cipherBytes = Base64.getDecoder().decode(b64Text);

            // 3) AES-CBC-PKCS7 解密
            byte[] keyBytes = aesKey.getBytes(StandardCharsets.UTF_8);
            byte[] ivBytes = aesIv.getBytes(StandardCharsets.UTF_8);

            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            return cipher.doFinal(cipherBytes);
        } catch (Exception e) {
            throw new RuntimeException("AES decryption failed", e);
        }
    }
}

