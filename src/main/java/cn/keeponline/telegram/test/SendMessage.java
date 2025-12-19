package cn.keeponline.telegram.test;

import cn.hutool.core.net.url.UrlQuery;
import cn.keeponline.telegram.config.Constants;
import cn.keeponline.telegram.dto.*;
import cn.keeponline.telegram.utils.SafeFileMd5Changer;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static cn.keeponline.telegram.test.YWebSocket.connectWebSocket;


@Slf4j
@RestController
@RequestMapping("/noviceTaskManage")
public class SendMessage {
    private static final String[] KEY_ARRAY = new String[]{
            "9Kz?8<e?OBkD9QXt",
            "?3L1cx4000rFfz8L",
            "R0aaGOQk0cfVhS!y",
            "R_I0DP7GO60VOHif",
            "_wvf7Lx2G5fA0p!O",
            "jz4Sh9ygLc30X1AY",
            "k0]P09ecYYMHadsM",
            "pmo0Ehnh0eOZ9X14",
            "wUtt_85]aOC0]xLO",
            "zFRzq8!eKHm0R4tX"
    };


    public static Map<String, String> w = new HashMap<>();

    static {
        w.put("deviceid", "Browser");
        w.put("deivice_name", "Browser"); // 保留原键名
        w.put("device_type", "BROWSER");
        w.put("terminal_type", "BROWSER");
        w.put("os_version", "5.0.0");
        w.put("platform", "win");
        w.put("arch", "32");
        w.put("version", "0-9-0");
    }

    public static class ParsedPacket {
        public final int length;
        public final int type;
        public final int version;
        public final int ext;
        public final int bodyLen;
        public final byte[] body;

        public ParsedPacket(int length, int type, int version, int ext, int bodyLen, byte[] body) {
            this.length = length;
            this.type = type;
            this.version = version;
            this.ext = ext;
            this.bodyLen = bodyLen;
            this.body = body;
        }
    }

    public static class DecryptResult {
        public final int type;
        public final int version;
        public final byte[] data;

        public DecryptResult(int type, int version, byte[] data) {
            this.type = type;
            this.version = version;
            this.data = data;
        }
    }

    private static byte[] deriveKeyBytes(int version) {
        String key = KEY_ARRAY[version - 1];
        byte[] k = key.getBytes(StandardCharsets.UTF_8);
        if (k.length > 16) {
            byte[] t = new byte[16];
            System.arraycopy(k, 0, t, 0, 16);
            return t;
        } else if (k.length < 16) {
            byte[] t = new byte[16];
            System.arraycopy(k, 0, t, 0, k.length);
            return t;
        }
        return k;
    }

    // 加密request
    public static byte[] ie(String typeStr, int version, byte[] data) throws Exception {
        int type = Integer.parseInt(typeStr);
        byte[] key = deriveKeyBytes(version);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec sk = new SecretKeySpec(key, "AES");
        IvParameterSpec iv = new IvParameterSpec(key);
        cipher.init(Cipher.ENCRYPT_MODE, sk, iv);
        byte[] encrypted = cipher.doFinal(data);

        int length = 16 + encrypted.length;
        ByteBuffer buf = ByteBuffer.allocate(length);
        buf.putInt(length);
        buf.putShort((short) type);
        buf.putShort((short) version);
        buf.putInt(0);
        buf.putInt(encrypted.length);
        buf.put(encrypted);
        return buf.array();
    }

    public static ParsedPacket ee(byte[] data) {
        if (data == null || data.length < 16) {
            return new ParsedPacket(data == null ? 0 : data.length, 0, 1, 0, data == null ? 0 : data.length, data == null ? new byte[0] : data);
        }
        ByteBuffer buf = ByteBuffer.wrap(data);
        int length = buf.getInt();
        int type = Short.toUnsignedInt(buf.getShort());
        int version = Short.toUnsignedInt(buf.getShort());
        int ext = buf.getInt();
        int bodyLen = buf.getInt();
        byte[] body = new byte[Math.max(0, Math.min(bodyLen, data.length - 16))];
        System.arraycopy(data, 16, body, 0, body.length);
        return new ParsedPacket(length, type, version, ext, bodyLen, body);
    }

    // 解密response
    public static Object te(byte[] encryptedData, boolean returnString) throws Exception {
        ParsedPacket p = ee(encryptedData);
        if (p.type == 0) {
            return returnString ? new String(p.body, StandardCharsets.UTF_8) : p.body;
        }
        byte[] key = deriveKeyBytes(p.version);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec sk = new SecretKeySpec(key, "AES");
        IvParameterSpec iv = new IvParameterSpec(key);
        cipher.init(Cipher.DECRYPT_MODE, sk, iv);
        byte[] decrypted = cipher.doFinal(p.body);
        return returnString ? new String(decrypted, StandardCharsets.UTF_8) : decrypted;
    }

    // 解密request
    public static DecryptResult de(byte[] data) throws Exception {
        if (data == null || data.length < 16) {
            throw new IllegalArgumentException("数据长度不足");
        }
        ByteBuffer buf = ByteBuffer.wrap(data, 0, 16);
        int length = buf.getInt();
        int type = Short.toUnsignedInt(buf.getShort());
        int version = Short.toUnsignedInt(buf.getShort());
        int reserved = buf.getInt();
        int originalLength = buf.getInt();

        byte[] key = deriveKeyBytes(version);
        byte[] encryptedBody = new byte[Math.max(0, Math.min(originalLength, length - 16))];
        System.arraycopy(data, 16, encryptedBody, 0, encryptedBody.length);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec sk = new SecretKeySpec(key, "AES");
        IvParameterSpec iv = new IvParameterSpec(key);
        cipher.init(Cipher.DECRYPT_MODE, sk, iv);
        byte[] decrypted = cipher.doFinal(encryptedBody);
        return new DecryptResult(type, version, decrypted);
    }

    public static String sendRequest(String body, String encry, String encryType, String url) throws Exception {
        String encryptedData = body;
        if ("1".equals(encry)) {
            byte[] packed = ie(encryType, 1, body.getBytes(StandardCharsets.UTF_8));
            encryptedData = Base64.getEncoder().encodeToString(packed);
//            System.out.println(encryptedData);
        }

        String form = "req=" + URLEncoder.encode(encryptedData, StandardCharsets.UTF_8);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", "UneedGroup/3.8.0 BROWSER/8.5.5 (win32-x64;Release;Build-0-9-0;8e3759346fa5c045964afe675751d93c7305f4bc2926eab3a6da242c716bd6fe)")
                .header("encrypt-type", encryType)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
//        System.out.println("Request successful: " + response.statusCode());
//        System.out.println("Response data length: " + (response.body() == null ? 0 : response.body().length));
        return te(response.body(), true).toString();
    }


    public static String getQrcode() throws Exception {
        String url = "https://api.uneed.com/v2/web/login?appid=yuni_web";
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                // 注意：Java 客户端会自动设置 Host/authority，通常无需手动设置
                .header("accept", "application/json, text/plain, */*")
                .header("user-agent", "UneedGroup/3.8.0 BROWSER/8.5.5 (win32-x64;Release;Build-0-9-0;8e3759346fa5c045964afe675751d93c7305f4bc2926eab3a6da242c716bd6fe)")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public static String pollStatus(String uuid) throws IOException, InterruptedException {
        String deviceJson = """
                {"ua":"UneedGroup/3.2.0 BROWSER/8.5.5 (win32-x64;Release;Build-0-9-0)","deviceid":"Browser","device_name":"win32-x64/Build-0-9-0"}
                """.trim();
        String deviceEnc = URLEncoder.encode(deviceJson, StandardCharsets.UTF_8);
        String url = "https://api.uneed.com/v2/web/poll?uuid=" + uuid + "&device=" + deviceEnc;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                // 注意：Java 客户端会自动设置 Host/authority，通常无需手动设置
                .header("accept", "application/json, text/plain, */*")
                .header("user-agent", "UneedGroup/3.8.0 BROWSER/8.5.5 (win32-x64;Release;Build-0-9-0;8e3759346fa5c045964afe675751d93c7305f4bc2926eab3a6da242c716bd6fe)")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }


    /**
     * 计算消息的MD5哈希值，支持多种输出格式
     *
     * @param message 要计算哈希的消息
     * @param options 选项字典，支持asBytes和asString参数
     * @return 根据选项返回字节数组、字符串或十六进制哈希值
     * @throws IllegalArgumentException 当消息为null时抛出
     */
    public static Object md5Hash(Object message, Map<String, Boolean> options) {
        if (message == null) {
            throw new IllegalArgumentException("Illegal argument: message cannot be null");
        }

        byte[] messageBytes;

        // 确保消息是字节类型
        if (message instanceof String) {
            messageBytes = ((String) message).getBytes(StandardCharsets.UTF_8);
        } else if (message instanceof byte[]) {
            messageBytes = (byte[]) message;
        } else {
            throw new IllegalArgumentException("Unsupported message type: " + message.getClass().getName());
        }

        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digestBytes = md5.digest(messageBytes);

            // 根据选项返回不同格式
            if (options != null) {
                if (Boolean.TRUE.equals(options.get("asBytes"))) {
                    return digestBytes;
                } else if (Boolean.TRUE.equals(options.get("asString"))) {
                    return new String(digestBytes, StandardCharsets.ISO_8859_1);
                }
            }

            // 默认返回十六进制字符串
            return bytesToHex(digestBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    /**
     * 将字节数组转换为十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * 便捷方法：只传入消息，返回十六进制字符串
     */
    public static String md5Hash(String message) {
        return (String) md5Hash(message, null);
    }


    public static JSONObject getUserInfo(String token) throws Exception {
        String device_sign = md5Hash(w.getOrDefault("deviceid", "Browser") + w.getOrDefault("device_type", "BROWSER"));
        String param = """
                {"session_meta": {"device_sign": "{0}"}, "params": {"uid": ""}}
                """.replace("{0}", device_sign);
        String url = "https://api.uneed.com/v2/profile/view/1000000000?token=" + token;
        return JSON.parseObject(sendRequest(param, "1", "1", url));
    }

    public static JSONObject getAccessToken(String uid, String token) throws Exception {
        String device_sign = md5Hash(uid +  w.getOrDefault("deviceid", "Browser") + w.getOrDefault("device_type", "BROWSER"));
        String param = """
                {"session_meta": {"uid": "{0}", "device_sign": "{1}}"}, "params": {"uid": "{0}}", "gameid": "nimo-web"}}
                """
                .replace("{0}", uid)
                .replace("{1}", device_sign);

        String url = "https://api.uneed.com/v2/lobby/gas/wire/" + uid + "?token=" + token;
        return JSON.parseObject(sendRequest(param, "1", "1", url));
    }

    public static List<GroupDTO> getGroupList(String uid, String token) throws Exception {
        String device_sign = md5Hash(uid +  w.getOrDefault("deviceid", "Browser") + w.getOrDefault("device_type", "BROWSER"));
        String param = """
                {"session_meta": {"uid": "{0}}", "device_sign": "{1}}"}, "params": {"pno": 0, "page_size": 100}}
                """
                .replace("{0}", uid)
                .replace("{1}", device_sign);

        String url = Constants.API_ADDRESS + "/v2/group/list/" + uid + "?token=" + token;
        String result = sendRequest(param, "1", "1", url);
//        log.info("群组完整信息: {}", result);
        return JSON.parseObject(result, new TypeReference<YResponse<GroupInfo>>() {
        }).getData().getGroups();
    }

    public static String getGroupListCheck(String uid, String token) throws Exception {
        String device_sign = md5Hash(uid +  w.getOrDefault("deviceid", "Browser") + w.getOrDefault("device_type", "BROWSER"));
        String param = """
                {"session_meta": {"uid": "{0}}", "device_sign": "{1}}"}, "params": {"pno": 0, "page_size": 100}}
                """
                .replace("{0}", uid)
                .replace("{1}", device_sign);

        String url = Constants.API_ADDRESS + "/v2/group/list/" + uid + "?token=" + token;
        return sendRequest(param, "1", "1", url);
    }

    public static List<FriendDTO> getFriendList(String uid, String token) throws Exception {
        String device_sign = md5Hash(uid +  w.getOrDefault("deviceid", "Browser") + w.getOrDefault("device_type", "BROWSER"));
        String param = """
                {"session_meta": {"uid": "{0}}", "device_sign": "{1}}"}, "params": {"pno": 0, "page_size": 100}}
                """
                .replace("{0}", uid)
                .replace("{1}", device_sign);

        String url = Constants.API_ADDRESS + "/v2/relation/list/friend/" + uid + "?token=" + token;
        String result = sendRequest(param, "1", "1", url);
        return JSON.parseObject(result, new TypeReference<YResponse<FriendInfo>>() {
        }).getData().getFriends();
    }


    public JSONObject getPictureRoute(String uid, String token, String md5, File tempFile, Long file_size, String file_name) throws Exception {
        return getPictureRoute(uid, token, md5, tempFile, file_size, file_name, false);
    }

    private JSONObject getPictureRoute(String uid, String token, String md5, File tempFile, Long file_size, String file_name, boolean retried) throws Exception {
        String device_sign = md5Hash(uid +  w.getOrDefault("deviceid", "Browser") + w.getOrDefault("device_type", "BROWSER"));
        String param = """
                {
                    "session_meta": {
                        "uid": "{uid}",
                        "device_sign": "{device_sign}"
                    },
                    "params": {
                        "uid": "{uid}",
                        "business_type": 2,
                        "upload_flag": 1,
                        "business_id": "1179466444509814784",
                        "content_type": "image/png",
                        "basic_info": {
                            "modify_time": {timestamp},
                            "file_name": "{file_name}",
                            "file_size": {file_size}
                        },
                        "file_md5": "{md5}",
                        "oss_direct": true
                    }
                }
                """
                .replace("{uid}", uid)
                .replace("{device_sign}", device_sign)
                .replace("{timestamp}", new Date().getTime() + "")
                .replace("{file_name}", file_name)
                .replace("{file_size}", file_size + "")
                .replace("{md5}", md5)
                ;
//        log.info("【yuni】第一步入参: {}", param);
        String url = Constants.API_ADDRESS + "/v2/file/route/" + uid + "?token=" + token;
        String result = sendRequest(param, "1", "1", url);
        JSONObject jsonObject = JSON.parseObject(result);
//        log.info("第一步返回值: {}", jsonObject);
        Thread.sleep(2000);
        url = jsonObject.getJSONObject("data").getString("url");
        String signature = UrlQuery.of(url, null).get("signature").toString();
//        log.info("返回的url: {}, signature: {}", url, signature);

        url = url + "&token=" + token;
//        log.info("第二步入参: {}", url);
        String s = sendRequest(param, "1", "1", url);
//        log.info("第二步返回值: {}", s);
        JSONObject jsonObject1 = JSON.parseObject(s);
        String refreshURL = jsonObject1.getJSONObject("data").getString("refresh_url");
        Boolean file_exists = jsonObject1.getJSONObject("data").getBoolean("file_exists");
        String info_url = jsonObject1.getJSONObject("data").getString("info_url");
        refreshURL = refreshURL.replaceAll("u0026", "&").replaceAll("\\\\", "");
        String expire = UrlQuery.of(refreshURL, null).get("expire").toString();
        signature = UrlQuery.of(refreshURL, null).get("signature").toString();
        jsonObject1 = jsonObject1.getJSONObject("data").getJSONObject("upload_credits");
        String objectkey = jsonObject1.getString("objectkey");
//        log.info("objectkey: {}", objectkey);
        String bucket = jsonObject1.getString("bucket");
        jsonObject1 = jsonObject1.getJSONObject("credits");
        String securityToken = jsonObject1.getString("SecurityToken");
        String Regionid = jsonObject1.getString("Regionid");

        long upload_time = new Date().getTime();
        if (!file_exists) {
            url = "https://upload-{region}.qiniup.com/".replace("{region}", Regionid);
            // 实际项目中，这些值通常是你自己服务器生成的 uploadToken 与 key
            // 构造表单数据
//            log.info("qiUrl: {}", url);
            // 发送 POST 请求
            cn.hutool.http.HttpResponse qiResponse = cn.hutool.http.HttpRequest.post(url)
                    .header("User-Agent", "UneedGroup/3.8.0 BROWSER/11.5.0 (darwin-x64;Release;Build-0-9-0)")
                    .header("Content-Type", "multipart/form-data")
                    .form("file", tempFile)
                    .form("token", securityToken)
                    .form("key", objectkey)
                    .form("fname", file_name)
                    .header("Accept", "*/*")
                    .charset(StandardCharsets.UTF_8)
                    .timeout(120000)
                    .execute();

            int status = qiResponse.getStatus();
            String body = qiResponse.body();
//            log.info("status: {}, body: {}", status, body);
        }
        url = """
            https://ups-qn-bj.uneedx.com/v2/upload/urls
            ?bucket={bucket}
            &business_id=1179466444509814784
            &business_type=2
            &content_type=image%2Fpng
            &encrypt=false
            &encrypt_iv=
            &encrypt_key=
            &expire={expire}
            &file_md5={md5}
            &file_name={file_name}
            &file_size={file_size}
            &key_version=
            &objectkey={objectkey}
            &oss_direct=true
            &region={region}
            &uid={uid}
            &upload_time={upload_time}
            &vip_archived=false
            &signature={signature}
            &token={token}
            """.stripIndent()
                .replace("\n", "")
                .replace("{bucket}", bucket)
                .replace("{expire}", expire)
                .replace("{md5}", md5)
                .replace("{file_name}", file_name)
                .replace("{file_size}", file_size + "")
                .replace("{objectkey}", objectkey)
                .replace("{region}", Regionid)
                .replace("{uid}", uid)
                .replace("{upload_time}", upload_time / 1000 + "")
                .replace("{signature}", signature)
                .replace("{token}", token);
        if (file_exists) {
            url = info_url + "&token=" + token;
        }
//        log.info("第四步入参: {}", url);
        Thread.sleep(1000L);
        cn.hutool.http.HttpResponse pictureInfoResponse = cn.hutool.http.HttpRequest.get(url)
                .header("authority", "ups-qn-bj.uneedx.com")
                .header("accept", "application/json, text/plain, */*")
                .header("user-agent", "UneedGroup/3.8.0 BROWSER/11.5.0 (darwin-x64;Release;Build-0-9-0;7c435ebea5b826bae7924c9ec8cd70d515b528a5a2029c93d7109343f05de934)")
                .header("encrypt-type", "1")
                .header("sec-fetch-site", "cross-site")
                .header("sec-fetch-mode", "cors")
                .header("sec-fetch-dest", "empty")
                .header("accept-language", "zh-CN")
                .timeout(30000)
                .execute();
        JSONObject data = JSON.parseObject(pictureInfoResponse.body()).getJSONObject("data");
//        log.info("信息: {}", JSON.toJSONString(data));
        Integer width = data.getJSONObject("basic_info").getInteger("width");
        if (width == null) {
            log.info("返回的width为空，重新上传照片");
            if (retried) {
                log.warn("width仍为空，已重试过一次，停止递归返回");
                return data;
            }
            SafeFileMd5Changer.Result pictureInfo = SafeFileMd5Changer.changeMd5Safely(tempFile);
            // 如果图片的width没有返回，重新走一遍上传的逻辑
            return getPictureRoute(uid, token, pictureInfo.getMd5(), pictureInfo.getFile(), pictureInfo.getSize(), UUID.randomUUID() + ".png", true);
        }

        return data;
    }

    public static JSONObject getPictureRoute2(String uid, String token, String md5, String fileName, Long fileSize) throws Exception {
        String device_sign = md5Hash(uid +  w.getOrDefault("deviceid", "Browser") + w.getOrDefault("device_type", "BROWSER"));
        String param = """
                {
                    "session_meta": {
                        "uid": "{uid}",
                        "device_sign": "{device_sign}"
                    },
                    "params": {
                        "uid": "{uid}",
                        "business_type": 2,
                        "upload_flag": 1,
                        "business_id": "1179466444509814784",
                        "content_type": "image/png",
                        "basic_info": {
                            "modify_time": {timestamp},
                            "file_name": "{file_name}",
                            "file_size": {file_size}
                        },
                        "file_md5": "{md5}",
                        "oss_direct": true
                    }
                }
                """
                .replace("{uid}", uid)
                .replace("{device_sign}", device_sign)
                .replace("{timestamp}", new Date().getTime() + "")
                .replace("{file_name}", fileName)
                .replace("{file_size}", fileSize + "")
                .replace("{md5}", md5)
                ;
//        log.info("【yuni】第一步入参: {}", param);
        String url = Constants.API_ADDRESS + "/v2/file/route/" + uid + "?token=" + token;
        String result = sendRequest(param, "1", "1", url);
        JSONObject jsonObject = JSON.parseObject(result);
//        log.info("第一步返回值: {}", jsonObject);
        Thread.sleep(2000);
        url = jsonObject.getJSONObject("data").getString("url");
        String signature = UrlQuery.of(url, null).get("signature").toString();
//        log.info("返回的url: {}, signature: {}", url, signature);

        url = url + "&token=" + token;
//        log.info("第二步入参: {}", url);
        String s = sendRequest(param, "1", "1", url);
//        log.info("第二步返回值: {}", s);
        JSONObject jsonObject1 = JSON.parseObject(s);
        String refreshURL = jsonObject1.getJSONObject("data").getString("refresh_url");
        Boolean file_exists = jsonObject1.getJSONObject("data").getBoolean("file_exists");
        String info_url = jsonObject1.getJSONObject("data").getString("info_url");
        refreshURL = refreshURL.replaceAll("u0026", "&").replaceAll("\\\\", "");
        String expire = UrlQuery.of(refreshURL, null).get("expire").toString();
        signature = UrlQuery.of(refreshURL, null).get("signature").toString();
        jsonObject1 = jsonObject1.getJSONObject("data").getJSONObject("upload_credits");
        String objectkey = jsonObject1.getString("objectkey");
        log.info("objectkey: {}", objectkey);
        String bucket = jsonObject1.getString("bucket");
        jsonObject1 = jsonObject1.getJSONObject("credits");
        String securityToken = jsonObject1.getString("SecurityToken");
        String Regionid = jsonObject1.getString("Regionid");

        long upload_time = new Date().getTime();

        url = """
            https://ups-qn-bj.uneedx.com/v2/upload/urls
            ?bucket={bucket}
            &business_id=1179466444509814784
            &business_type=2
            &content_type=image%2Fpng
            &encrypt=false
            &encrypt_iv=
            &encrypt_key=
            &expire={expire}
            &file_md5={md5}
            &file_name={file_name}
            &file_size={file_size}
            &key_version=
            &objectkey={objectkey}
            &oss_direct=true
            &region={region}
            &uid={uid}
            &upload_time={upload_time}
            &vip_archived=false
            &signature={signature}
            &token={token}
            """.stripIndent()
                .replace("\n", "")
                .replace("{bucket}", bucket)
                .replace("{expire}", expire)
                .replace("{md5}", md5)
                .replace("{file_name}", fileName)
                .replace("{file_size}", fileSize + "")
                .replace("{objectkey}", objectkey)
                .replace("{region}", Regionid)
                .replace("{uid}", uid)
                .replace("{upload_time}", upload_time / 1000 + "")
                .replace("{signature}", signature)
                .replace("{token}", token);
        if (file_exists) {
            url = info_url + "&token=" + token;
        }
//        log.info("第四步入参: {}", url);
        cn.hutool.http.HttpResponse pictureInfoResponse = cn.hutool.http.HttpRequest.get(url)
                .header("authority", "ups-qn-bj.uneedx.com")
                .header("accept", "application/json, text/plain, */*")
                .header("user-agent", "UneedGroup/3.8.0 BROWSER/11.5.0 (darwin-x64;Release;Build-0-9-0;7c435ebea5b826bae7924c9ec8cd70d515b528a5a2029c93d7109343f05de934)")
                .header("encrypt-type", "1")
                .header("sec-fetch-site", "cross-site")
                .header("sec-fetch-mode", "cors")
                .header("sec-fetch-dest", "empty")
                .header("accept-language", "zh-CN")
                .timeout(30000)
                .execute();
//        JSONObject data = JSON.parseObject(pictureInfoResponse.body()).getJSONObject("data");
//        log.info("信息: {}", JSON.toJSONString(data));

        return JSON.parseObject(pictureInfoResponse.body()).getJSONObject("data");
    }

    public static String removePort(String url) {
        return url.replaceAll("^(ws|wss)://(\\[[^\\]]+\\]|[^/:]+):\\d+(?=/|$)", "$1://$2");
    }


    public static void main(String[] args) throws Exception {
        DecryptResult de = de(Base64.getDecoder().decode("AAABsAABAAEAAAAAAAABoJHleQwQNpTDiYl7D2vvww9IC/eDEoDfioScd/ZOoYkzJEiVfDlw8osJh/TJE1AZ0wBS9//ENBLuG7O2/HDVKxYdN+GbWUwzRHQRnar80lEynd9t+oF3l0yPXRuF1o7KDZzeNAQNudIXBtjjR0Us006Q3MEcPEXXRSgCr3wNUrhtRyod0n3JyNtRisp9iqqNVoNPxrdcoNqZnUyBuSWFc+ISr35yHmNkMmjKL2ViAtdSpNh9p8yuJOO/vawEqm09pc6J/x24vBqAGe25ytPUnqsCMVenAPO0Mt+P/rylUtdawPdWmKMW9hYZYRayIzKZbWg4vo4PCrNRC2r7KFLibwcnTkQoMt5SEcNKXGIFWDhvQUrOzer2ymjDXQ12kIL89G1z6cNRN5jkknLUyu590GV2g9ORquAncVaxg6VhBpvvR6EBvZSwo4tsuaIXj+zNL6c8WCDZV4JWEKq5wI06nzHAjx2D1/DzVDDsbCs7mz4jk+Ns9a621PBBTeWmBxB2yvauNEz5wrJMNMiqlHvsREm0zC5ecaDyZBulLolQvvOu"));
        System.out.println(new String(de.data));
        String qrcode = getQrcode();
        System.out.println("【yuni】qrcode:" + qrcode);
        JSONObject qrcodeObject = JSONObject.parseObject(qrcode);
        String uuid = qrcodeObject.getJSONObject("data").getString("uuid");
        JSONObject poll;

        while (true) {
            Thread.sleep(3000);
            String s = pollStatus(uuid);
            poll = JSONObject.parseObject(s);
            System.out.println("poll: " + poll);
            if (poll.getJSONObject("data").getInteger("status") == 2) {
                System.out.println("【yuni】login success");
                break;
            }
        }
//        System.out.println(JSON.toJSONString(poll));

        String loginToken = poll.getJSONObject("data").getString("token");
        System.out.println("【yuni】loginToken:" + loginToken);

        JSONObject userInfo = getUserInfo(loginToken);
        System.out.println("【yuni】userInfo" + userInfo);

        String uid = userInfo.getJSONObject("data").getString("uid");
        System.out.println("【yuni】uid:" + uid);

        JSONObject wssInfo = getAccessToken(uid, loginToken);

        String wssUrl = removePort(wssInfo.getJSONObject("data").getString("addr").trim());

        //连接websocket
        connectWebSocket(wssUrl, uid, wssInfo.getJSONObject("data").getString("access_token"));

//        if (websocket.getWebSocket() != null) {
        if (true) {

            List<GroupDTO> groupList = getGroupList(uid, loginToken);
            System.out.println("【yuni】groupList: " + JSON.toJSONString(groupList));
//            JSONArray groups = groupList.getJSONObject("data").getJSONArray("groups");
            for (GroupDTO groupDTO : groupList) {
                String gid = groupDTO.getGid();
                String groupMessage = """
                        {"header":{"remoteid":"{gid}","cvsid":"{gid}","uid":"{uid}","status":1,"cmdtype":"g.gmsg","opid":{opid}},"body":{"msg_head":{"cvs_type":2,"createtime":{createtime},"msgid":"{msgid}","msg_type":1,"chat_profiles":{},"is_display":true},"msg_body":{"progress":0,"text":"{messageText}","mentions":{},"instructions":[]}}}
                        """
                        .replace("{gid}", gid)
                        .replace("{uid}", uid)
                        .replace("{opid}", "0")
                        .replace("{createtime}", new Date().getTime() + "")
                        .replace("{msgid}", UUID.randomUUID().toString())
                        .replace("{messageText}", "hello world");
                System.out.println(groupMessage);
                Thread.sleep(5000);
                YWebSocket.getWebSocket().send(groupMessage);
                System.out.println("📤 已发送: " + groupMessage);
            }
        }

        if (YWebSocket.getWebSocket() != null) {
            YWebSocket.getWebSocket().close(3000, "程序执行完成");
        }
    }


}
