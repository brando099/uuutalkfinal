package cn.keeponline.telegram.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {

    public static String convert(String oldPassword) {
        try {
            // Using UTF-8 encoding
            byte[] bytes = oldPassword.getBytes(StandardCharsets.UTF_8);

            // Calculating MD5 hash
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(bytes);

            // Converting the MD5 hash to a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                hexString.append(String.format("%02x", b & 0xff));
            }

            // Creating a new password by appending the original password, MD5 hash, and original password
            String newPassword = oldPassword + hexString.toString() + oldPassword;

            // Calculating MD5 hash for the new password
            byte[] newDigest = md5.digest(newPassword.getBytes(StandardCharsets.UTF_8));

            // Converting the new MD5 hash to a hexadecimal string
            StringBuilder newHexString = new StringBuilder();
            for (byte b : newDigest) {
                newHexString.append(String.format("%02x", b & 0xff));
            }

            return newHexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Handle exception (e.g., print an error message)
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        String oldPassword = "Aa111111";
        String hashedPassword = convert(oldPassword);
        System.out.println("Original Password: " + oldPassword);
        System.out.println("Hashed Password: " + hashedPassword);
    }
}
