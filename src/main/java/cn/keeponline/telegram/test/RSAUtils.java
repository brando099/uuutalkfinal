package cn.keeponline.telegram.test;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


public class RSAUtils {

    // 使用私钥进行签名
    public static String sign(String data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes("UTF-8"));
        byte[] signedData = signature.sign();
        return Base64.getEncoder().encodeToString(signedData);
    }

    // 使用公钥验证签名
    public static boolean verify(String data, String signatureStr, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initVerify(publicKey);
        signature.update(data.getBytes("UTF-8"));
        byte[] signatureBytes = Base64.getDecoder().decode(signatureStr);
        return signature.verify(signatureBytes);
    }

    // 从字符串中加载私钥
    public static PrivateKey getPrivateKeyFromString(String key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return keyFactory.generatePrivate(keySpec);
    }

    // 从字符串中加载公钥
    public static PublicKey getPublicKeyFromString(String key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        return keyFactory.generatePublic(keySpec);
    }

    public static void main(String[] args) throws Exception {
        // 生成的公钥和私钥字符串（密钥长度为1024，格式为PKCS#1）
        String privateKeyStr =
                "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALP6KD9v6VgaHieG" +
                "qZfZrDOmAi5zjBrv407G9NamJvFnLp1KEnn9Uhrh8k5f4WlI87L8sBJYkZzSxRIh" +
                "EVjh3lWvEMRcBJWg9sikWKFD4hiFGWDUACldddjYg44Sg5oNuhnysKV9sEMkeg+j" +
                "OGgcSCXdQ68E0+hbzUDkgo8ZN6f3AgMBAAECgYBmNeRTmpfXjAWiBu0bqugL8PR+" +
                "MurXhXL/EyiLGpM1N5cWhrGVjBLr53O3pOYb5+1WsgDAeoI/oMeAszzp0GR2KZWi" +
                "LB01rZc7/Q1j/TRoNR77BXcjihj+wT0UrnufXrDFU4VWW/LDkT61Xp9sFBcgFvF0" +
                "79KDw/W09NH4c+VbqQJBAO9M2UCHcvF5jcMvTvx19VSriXMBF0aJMd9SnjbDsb0A" +
                "dSWF3gH0mhoYJBMQ97zjitkHHeUKLJyW7ra4AVipbN0CQQDAiX1cRaL8b7CQTjO6" +
                "kViBYl/W5sifLZOflwKVeRAeC6qQOLxmjB1WVGu//lDzD1CEqaf1OPo7b63QSFHi" +
                "mqDjAkAy46Pb3jIqehAUrw3cEHXAsM4FH/lELc7mUBqHSOyWZe+DsEk7HzpaTEH3" +
                "sAcPK1COwL2xxI0iK9LOFqlqonUdAkEAt6HqaJkaLD2yXs/XUnfRvAVBd0vByN/F" +
                "To7OrhU7JAzobolOV1gHmxEFe6ZpOok+uGi/gokHUKzhUCfJwaDNFQJBAM9O/DYP" +
                "jtoQC7VNv73NoJ7Gmp/PtmGoWJnDzYTmL4nWa8yepWQVFiyXyuWSYicF6X76vozr" +
                "YtCAenFbr2ZScLU=";

        String publicKeyStr =
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCz+ig/b+lYGh4nhqmX2awzpgIu" +
                "c4wa7+NOxvTWpibxZy6dShJ5/VIa4fJOX+FpSPOy/LASWJGc0sUSIRFY4d5VrxDE" +
                "XASVoPbIpFihQ+IYhRlg1AApXXXY2IOOEoOaDboZ8rClfbBDJHoPozhoHEgl3UOv" +
                "BNPoW81A5IKPGTen9wIDAQAB";

        // 测试的数据
        String data = "需要签名的数据";

        // 获取私钥和公钥
        PrivateKey privateKey = getPrivateKeyFromString(privateKeyStr);
        PublicKey publicKey = getPublicKeyFromString(publicKeyStr);

        // 使用私钥签名
        String signature = sign(data, privateKey);
        System.out.println("签名: " + signature);

        // 使用公钥验签
        boolean isVerified = verify(data, signature, publicKey);
        System.out.println("签名验证结果: " + isVerified);
    }
}