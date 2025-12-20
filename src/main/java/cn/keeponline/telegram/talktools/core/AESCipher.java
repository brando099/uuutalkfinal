package cn.keeponline.telegram.talktools.core;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES 加密/解密工具类
 * 完全统一加密/解密，等价于 JS v.shared() 里的 AES.encrypt / AES.decrypt
 */
public class AESCipher {
    private final byte[] aesKeyBytes;
    private final byte[] aesIvBytes;

    public AESCipher(String aesKey, String aesIv) {
        this.aesKeyBytes = aesKey.getBytes(StandardCharsets.UTF_8);
        this.aesIvBytes = aesIv.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * AES 加密（明文字符串 → Base64）
     * 对应 CryptoJS: AES.encrypt(Utf8.parse(plaintext), Utf8.parse(key), { iv: Utf8.parse(iv), mode: CBC, padding: Pkcs7 })
     */
    public String encryption(String plaintext) {
        try {
            byte[] ptBytes = plaintext.getBytes(StandardCharsets.UTF_8);

            SecretKeySpec keySpec = new SecretKeySpec(aesKeyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(aesIvBytes);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            byte[] ciphertext = cipher.doFinal(ptBytes);
            return Base64.getEncoder().encodeToString(ciphertext);
        } catch (Exception e) {
            throw new RuntimeException("AES encryption failed", e);
        }
    }

    /**
     * encryption2：Uint8Array → 字符串 → 加密
     * 对应 JS: String.fromCharCode.apply(null, Array.from(e)) → decodeURIComponent(escape(t)) → encryption(n)
     */
    public String encryption2(byte[] data) {
        String text = new String(data, StandardCharsets.UTF_8);
        return encryption(text);
    }

    /**
     * AES 解密（Base64 → 明文字节）
     * 对应 JS: b64_text = uintToString(Array.from(payload)) → AES.decrypt(Base64.parse(b64_text), key, iv, CBC)
     */
    public byte[] decryption(byte[] payloadBytes) {
        try {
            // Uint8Array → base64 字符串
            String b64Text = new String(payloadBytes, StandardCharsets.UTF_8);

            // Base64 解码
            byte[] cipherBytes = Base64.getDecoder().decode(b64Text);

            // AES-CBC 解密
            SecretKeySpec keySpec = new SecretKeySpec(aesKeyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(aesIvBytes);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            return cipher.doFinal(cipherBytes);
        } catch (Exception e) {
            throw new RuntimeException("AES decryption failed", e);
        }
    }
}

