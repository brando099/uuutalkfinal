package cn.keeponline.telegram.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;

public class UneedCrypto {

    // 对应 Python 的 key_array
    private static final String[] KEY_ARRAY = {
            "9Kz?8<e?OBkD9QXt", "?3L1cx4000rFfz8L", "R0aaGOQk0cfVhS!y", "R_I0DP7GO60VOHif",
            "_wvf7Lx2G5fA0p!O", "jz4Sh9ygLc30X1AY", "k0]P09ecYYMHadsM", "pmo0Ehnh0eOZ9X14",
            "wUtt_85]aOC0]xLO", "zFRzq8!eKHm0R4tX"
    };

    /**
     * AES-128-CBC 加密
     * 对应 Python 的 ie() 函数
     */
    public static byte[] encryptRequest(int type, int version, byte[] data) throws Exception {
        String keyStr = KEY_ARRAY[version - 1];
        byte[] key = fixKeyLength(keyStr.getBytes("UTF-8"));

        // PKCS5Padding 填充
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec(key);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);
        byte[] encrypted = cipher.doFinal(data);

        int length = 16 + encrypted.length;
        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(length);           // 4字节 length
        buffer.putShort((short) type);   // 2字节 type
        buffer.putShort((short) version);// 2字节 version
        buffer.putInt(0);                // 4字节 reserved/ext
        buffer.putInt(encrypted.length); // 4字节 body length
        buffer.put(encrypted);           // 加密内容

        return buffer.array();
    }

    /**
     * 解密 request
     * 对应 Python 的 de() 函数
     */
    public static String decryptRequest(byte[] data) throws Exception {
        if (data.length < 16) {
            throw new IllegalArgumentException("数据长度不足 16");
        }

        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
        int length = buffer.getInt();
        int type = buffer.getShort() & 0xFFFF;
        int version = buffer.getShort() & 0xFFFF;
        buffer.getInt(); // reserved
        int bodyLen = buffer.getInt();

        byte[] encrypted = new byte[bodyLen];
        buffer.get(encrypted);

        String keyStr = KEY_ARRAY[version - 1];
        byte[] key = fixKeyLength(keyStr.getBytes("UTF-8"));

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec(key);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);
        byte[] decrypted = cipher.doFinal(encrypted);

        return new String(decrypted, "UTF-8");
    }

    /**
     * 解密 response
     * 对应 Python 的 te() 函数
     */
    public static String decryptResponse(byte[] encryptedData) throws Exception {
        if (encryptedData.length < 16) {
            return "";
        }

        ByteBuffer buffer = ByteBuffer.wrap(encryptedData).order(ByteOrder.BIG_ENDIAN);
        int length = buffer.getInt();
        int type = buffer.getShort() & 0xFFFF;
        int version = buffer.getShort() & 0xFFFF;
        int ext = buffer.getInt();
        int bodyLen = buffer.getInt();

        byte[] body = new byte[bodyLen];
        buffer.get(body);

        if (type == 0) {
            return new String(body, "UTF-8");
        }

        String keyStr = KEY_ARRAY[version - 1];
        byte[] key = fixKeyLength(keyStr.getBytes("UTF-8"));

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec(key);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);
        byte[] decrypted = cipher.doFinal(body);

        return new String(decrypted, "UTF-8");
    }

    // 修正 key 长度为 16 字节
    private static byte[] fixKeyLength(byte[] key) {
        if (key.length == 16) return key;
        byte[] fixed = new byte[16];
        for (int i = 0; i < 16; i++) {
            fixed[i] = (i < key.length) ? key[i] : 0;
        }
        return fixed;
    }

    public static void main(String[] args) throws Exception {
        String json = "{\"hello\":\"world\"}";

        // 加密
//        byte[] encrypted = encryptRequest(1, 1, json.getBytes("UTF-8"));
//        System.out.println("加密后Base64: " + Base64.getEncoder().encodeToString(encrypted));

        // 解密
        String decrypted = decryptRequest(Base64.getDecoder().decode("AAAAoAABAAEAAAAAAAAAkJHleQwQNpTDiYl7D2vvww9IC/eDEoDfioScd/ZOoYkzJEiVfDlw8osJh/TJE1AZ0wBS9//ENBLuG7O2/HDVKxYdN+GbWUwzRHQRnar80lEynd9t+oF3l0yPXRuF1o7KDVv2I9TXuGJsDLqXleottmdcaBVyA7p6uulLEYxVL0+MkFNCBYlVLxZ3wp023DZY+Q=="));
        System.out.println("解密结果: " + decrypted);
    }
}