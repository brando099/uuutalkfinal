package cn.keeponline.telegram.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Base64;

public class AesDecryptor {

    private static final String[] KEY_ARRAY = {
            "9Kz?8<e?OBkD9QXt", "?3L1cx4000rFfz8L", "R0aaGOQk0cfVhS!y", "R_I0DP7GO60VOHif",
            "_wvf7Lx2G5fA0p!O", "jz4Sh9ygLc30X1AY", "k0]P09ecYYMHadsM", "pmo0Ehnh0eOZ9X14",
            "wUtt_85]aOC0]xLO", "zFRzq8!eKHm0R4tX"
    };

    public static class DecryptResult {
        public int type;
        public int version;
        public byte[] data;
    }

    public static DecryptResult decrypt(byte[] input) throws Exception {
        if (input.length < 16) {
            throw new IllegalArgumentException("数据长度不足");
        }

        // 解析包头 (大端)
        ByteBuffer buffer = ByteBuffer.wrap(input).order(ByteOrder.BIG_ENDIAN);
        int length = buffer.getInt();      // 4 bytes
        short type = buffer.getShort();    // 2 bytes
        short version = buffer.getShort(); // 2 bytes
        int reserved = buffer.getInt();    // 4 bytes
        int originalLen = buffer.getInt(); // 4 bytes

        // 提取密钥
        String keyStr = KEY_ARRAY[version - 1];
        byte[] key = Arrays.copyOf(keyStr.getBytes("UTF-8"), 16); // 确保16字节

        // 提取加密体
        byte[] encrypted = Arrays.copyOfRange(input, 16, length);

        // AES-CBC 解密
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec iv = new IvParameterSpec(key);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
        byte[] decrypted = cipher.doFinal(encrypted);

        // 封装结果
        DecryptResult result = new DecryptResult();
        result.type = type;
        result.version = version;
        result.data = decrypted;

        return result;
    }

    // 测试用例
    public static void main(String[] args) throws Exception {
        // 示例：假设有 Python 生成的加密二进制数据
        byte[] encrypted = Base64.getDecoder().decode("AAAAoAABAAEAAAAAAAAAkJHleQwQNpTDiYl7D2vvww9IC/eDEoDfioScd/ZOoYkzJEiVfDlw8osJh/TJE1AZ0wBS9//ENBLuG7O2/HDVKxYdN+GbWUwzRHQRnar80lEynd9t+oF3l0yPXRuF1o7KDVv2I9TXuGJsDLqXleottmdcaBVyA7p6uulLEYxVL0+MkFNCBYlVLxZ3wp023DZY+Q=="); // 你要解密的二进制数据

        DecryptResult result = decrypt(encrypted);
        System.out.println("Type: " + result.type);
        System.out.println("Version: " + result.version);
        System.out.println("Decrypted: " + new String(result.data, "UTF-8"));
    }
}