package cn.keeponline.telegram.talktools.services;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class OssMultipartUploader {

    // ✅ 使用真实 region endpoint（香港）
    private static final String ENDPOINT = "https://oss-cn-hongkong.aliyuncs.com";
    private static final String BUCKET_NAME = "im-aitalk-resource";

    /**
     * 对应 Python 的 multipart_upload_one_part(local_file, object_key)
     */
    public static Map<String, String> multipartUploadOnePart(
            String token,
            String localFilePath,
            String objectKey
    ) throws Exception {

        // 1️⃣ 获取 STS
        StsCacheEntry entry = getStsCached(token);
        Map<String, Object> sts = entry.sts;

        String accessKeyId = String.valueOf(sts.get("access_key_id"));
        String accessKeySecret = String.valueOf(sts.get("access_key_secret"));
        String securityToken = String.valueOf(sts.get("security_token"));

        // 2️⃣ 创建 OSS Client（STS）
        OSS oss = new OSSClientBuilder().build(
                ENDPOINT,
                accessKeyId,
                accessKeySecret,
                securityToken
        );

        String uploadId = null;
        String etag;

        try {
            // 3️⃣ InitMultipartUpload (?uploads)
            InitiateMultipartUploadRequest initReq =
                    new InitiateMultipartUploadRequest(BUCKET_NAME, objectKey);

            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentType(detectContentType(objectKey));
            meta.setCacheControl("max-age=31536000");
            initReq.setObjectMetadata(meta);

            InitiateMultipartUploadResult initRes = oss.initiateMultipartUpload(initReq);
            uploadId = initRes.getUploadId();
            System.out.println("uploadId = " + uploadId);

            // 4️⃣ UploadPart (?partNumber=1&uploadId=...)
            File file = new File(localFilePath);

            UploadPartRequest upReq = new UploadPartRequest();
            upReq.setBucketName(BUCKET_NAME);
            upReq.setKey(objectKey);
            upReq.setUploadId(uploadId);
            upReq.setPartNumber(1);
            upReq.setPartSize(file.length());

            PartETag partETag;
            try (InputStream in = new FileInputStream(file)) {
                upReq.setInputStream(in);
                UploadPartResult upRes = oss.uploadPart(upReq);
                partETag = upRes.getPartETag();
            }

            etag = partETag.getETag();
            System.out.println("part etag = " + etag);

            // 5️⃣ CompleteMultipartUpload (?uploadId=...)
            List<PartETag> partETags = new ArrayList<>();
            partETags.add(partETag);

            CompleteMultipartUploadRequest completeReq =
                    new CompleteMultipartUploadRequest(
                            BUCKET_NAME,
                            objectKey,
                            uploadId,
                            partETags
                    );

            oss.completeMultipartUpload(completeReq);
            System.out.println("complete ok");

            Map<String, String> ret = new HashMap<>();
            ret.put("uploadId", uploadId);
            ret.put("etag", etag);
            return ret;

        } catch (Exception e) {
            // ❌ 失败时 abort
            if (uploadId != null) {
                try {
                    oss.abortMultipartUpload(
                            new AbortMultipartUploadRequest(BUCKET_NAME, objectKey, uploadId)
                    );
                } catch (Exception ignore) {}
            }
            throw e;
        } finally {
            oss.shutdown();
        }
    }

    /**
     * 根据文件后缀识别 Content-Type
     */
    private static String detectContentType(String filename) {
        if (filename == null) return "application/octet-stream";
        String f = filename.toLowerCase(Locale.ROOT);

        if (f.endsWith(".png"))  return "image/png";
        if (f.endsWith(".jpg") || f.endsWith(".jpeg")) return "image/jpeg";
        if (f.endsWith(".gif"))  return "image/gif";
        if (f.endsWith(".webp")) return "image/webp";
        if (f.endsWith(".bmp"))  return "image/bmp";
        if (f.endsWith(".svg"))  return "image/svg+xml";

        if (f.endsWith(".mp4"))  return "video/mp4";
        if (f.endsWith(".mov"))  return "video/quicktime";
        if (f.endsWith(".avi"))  return "video/x-msvideo";

        if (f.endsWith(".mp3"))  return "audio/mpeg";
        if (f.endsWith(".wav"))  return "audio/wav";

        if (f.endsWith(".pdf"))  return "application/pdf";
        if (f.endsWith(".json")) return "application/json";
        if (f.endsWith(".txt"))  return "text/plain; charset=utf-8";

        return "application/octet-stream";
    }



    // ===== STS 内存缓存（按 token 维度）=====
    private static final long STS_REFRESH_BEFORE_SECONDS = 120; // 剩余 <=120s 认为快过期，刷新
    private static final Map<String, StsCacheEntry> STS_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    private static final class StsCacheEntry {
        final Map<String, Object> sts;
        final long expirationEpochSeconds;

        StsCacheEntry(Map<String, Object> sts, long expirationEpochSeconds) {
            this.sts = sts;
            this.expirationEpochSeconds = expirationEpochSeconds;
        }
    }

    private static StsCacheEntry getStsCached(String token) throws Exception {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("token is required for STS fetch/cache");
        }

        long now = java.time.Instant.now().getEpochSecond();
        StsCacheEntry cached = STS_CACHE.get(token);
        if (cached != null && (cached.expirationEpochSeconds - now) > STS_REFRESH_BEFORE_SECONDS) {
            return cached;
        }

        // double-check + 同 token 串行刷新，避免并发下重复请求
        synchronized (("STS_LOCK_" + token).intern()) {
            long now2 = java.time.Instant.now().getEpochSecond();
            StsCacheEntry cached2 = STS_CACHE.get(token);
            if (cached2 != null && (cached2.expirationEpochSeconds - now2) > STS_REFRESH_BEFORE_SECONDS) {
                return cached2;
            }

            UuutalkApiClient client = new UuutalkApiClient();
            Map<String, Object> sts = client.getFileSts(token);

            long expSec = extractExpirationEpochSeconds(sts);
            StsCacheEntry fresh = new StsCacheEntry(sts, expSec);
            STS_CACHE.put(token, fresh);
            return fresh;
        }
    }

    private static long extractExpirationEpochSeconds(Map<String, Object> sts) {
        if (sts == null) throw new IllegalArgumentException("sts is null");

        Object tsObj = sts.get("expiration_ts");
        if (tsObj != null) {
            try {
                return Long.parseLong(String.valueOf(tsObj));
            } catch (Exception ignore) {}
        }

        Object expObj = sts.get("expiration");
        if (expObj != null) {
            String exp = String.valueOf(expObj);
            // 兼容 "2025-12-16T10:42:36Z" 或带 offset
            try {
                return java.time.Instant.parse(exp).getEpochSecond();
            } catch (Exception ignore) {}
            try {
                return java.time.OffsetDateTime.parse(exp).toInstant().getEpochSecond();
            } catch (Exception ignore) {}
        }

        // 如果服务端没给 expiration，保守给一个很短 TTL，避免长期误用
        return java.time.Instant.now().getEpochSecond() + 60;
    }

    public static void clearStsCache(String token) {
        if (token == null) return;
        STS_CACHE.remove(token);
    }

    public static void clearAllStsCache() {
        STS_CACHE.clear();
    }

    // 示例 main
    public static void main(String[] args) throws Exception {
        String token = "e79acc9db1bb4012a10b6f4565954f63";
        String localPng = "/Users/momemtkes/Downloads/chrome/5992.png";
        String ossKey = "chat/2/d88d5141821740aeaa6366776f95dd50/1a3ba617f3384eb2978255a27ecdb7b7.png";

        multipartUploadOnePart(token, localPng, ossKey);
    }
}