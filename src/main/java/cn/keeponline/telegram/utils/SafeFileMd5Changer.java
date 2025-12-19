package cn.keeponline.telegram.utils;

import org.springframework.util.DigestUtils;

import java.io.*;
import java.nio.file.Files;
import java.security.SecureRandom;

public class SafeFileMd5Changer {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * ✅ 不管文件是真是假 PNG/JPG/WEBP，只要是二进制文件：
     * ✅ 追加极小随机字节，MD5 一定变化
     * ✅ 绝不破坏图片显示
     * ✅ 体积只增加 1～16 字节
     */
    public static Result changeMd5Safely(File src) throws Exception {
        if (!src.exists()) {
            throw new FileNotFoundException("文件不存在: " + src.getAbsolutePath());
        }

        // ✅ 先生成一个临时文件（防止直接污染目标名）
        String tmpName = "tmp_" + System.currentTimeMillis() + getExt(src.getName());
        File tmpFile = new File(src.getParent(), tmpName);

        // ✅ 1. 先完整复制原文件
        Files.copy(src.toPath(), tmpFile.toPath());

        // ✅ 2. 在文件尾部追加 1~16 个随机字节（改变 MD5）
        int appendLen = 1 + RANDOM.nextInt(16);
        byte[] tail = new byte[appendLen];
        RANDOM.nextBytes(tail);

        try (FileOutputStream fos = new FileOutputStream(tmpFile, true)) {
            fos.write(tail);
        }

        // ✅ 3. 计算“新文件”的 MD5
        String newMd5 = DigestUtils.md5DigestAsHex(
                Files.newInputStream(tmpFile.toPath())
        );

        // ✅ 4. 用 “新MD5 + 原扩展名” 作为最终文件名
        String finalName = newMd5 + getExt(src.getName());
        File finalFile = new File(src.getParent(), finalName);

        // ✅ 5. 重命名为最终文件名
        Files.move(
                tmpFile.toPath(),
                finalFile.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
        );

        // ✅ 6. 获取最终大小
        long size = Files.size(finalFile.toPath());

        return new Result(finalFile, newMd5, size);
    }

    private static String getExt(String name) {
        int i = name.lastIndexOf(".");
        return i == -1 ? "" : name.substring(i);
    }

    // ✅ 统一返回结果
    public static class Result {
        private final File file;
        private final String md5;
        private final long size;

        public Result(File file, String md5, long size) {
            this.file = file;
            this.md5 = md5;
            this.size = size;
        }

        public File getFile() {
            return file;
        }

        public String getMd5() {
            return md5;
        }

        public long getSize() {
            return size;
        }
    }

    // ✅ 直接 main 跑测试
    public static void main(String[] args) throws Exception {
        File src = new File("/Users/momemtkes/Downloads/telegram/223.png");

        Result result = changeMd5Safely(src);

        System.out.println("新文件: " + result.getFile().getAbsolutePath());
        System.out.println("新MD5 : " + result.getMd5());
        System.out.println("新大小: " + result.getSize());
    }
}