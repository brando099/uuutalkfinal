package cn.keeponline.telegram.talktools.services;

import cn.keeponline.telegram.dto.uuudto.UUUFriendDTO;
import cn.keeponline.telegram.dto.uuudto.UUUGroupDTO;
import cn.keeponline.telegram.dto.uuudto.UUUGroupVO;
import cn.keeponline.telegram.dto.uuudto.*;
import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.keeponline.telegram.talktools.logging.Logging;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

// WASM 相关导入（根据实际使用的 wasmtime-java 版本调整）
// import io.github.bytecodealliance.wasmtime.*;

/**
 * UUTalk API 客户端
 * 注意：WASM 签名功能需要额外实现（可通过 JNI 或 GraalVM）
 */
@Slf4j
public class UuutalkApiClient {
    private static final Logger logger = Logging.getLogger(UuutalkApiClient.class);
    public static String BASE_URL = "https://api.uutalk.co/v1";
    private static final String WEB_ORIGIN = "https://web.uuutalk.co";

    public static String REGION_URL = "https://region.uuutalk.cc/region.json";
    public static String REGION_URL_2 = "https://region-appconfig.oss-cn-beijing.aliyuncs.com/config/region.json";

    private final UuutalkConfig config;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    // WASM 签名器
    private WasmSigner wasmSigner;


    public UuutalkApiClient() {
        this(new UuutalkConfig());
    }

    public UuutalkApiClient(UuutalkConfig config) {
        this.config = config;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();

        if (config.getClientUuid() == null || config.getClientUuid().isEmpty()) {
            config.setClientUuid(UUID.randomUUID().toString());
        }

        // 初始化 WASM 签名器（延迟初始化）
        wasmSigner = new WasmSigner();
    }

    /**
     * 签名方法（基于 WASM 实现）
     */
    public String sign(String a, String timestamp, String nonce, String token) {
        try {
            return wasmSigner.sign(a, timestamp, nonce, token);
        } catch (Exception e) {
            logger.error("签名失败", e);
            return "placeholder_signature";
        }
    }

    /**
     * 自定义编码（等价 encodeURIComponent）
     */
    private String customEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20")
                    .replace("%21", "!")
                    .replace("%27", "'")
                    .replace("%28", "(")
                    .replace("%29", ")")
                    .replace("%7E", "~");
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * 构造参与签名的第一个参数 a
     */
    private String buildAString(String method, Map<String, String> params, Object data, String url) {
        method = method != null ? method.toUpperCase() : "GET";

        if ("GET".equals(method)) {
            if (params != null && !params.isEmpty()) {
                List<String> parts = new ArrayList<>();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    String kEnc = customEncode(entry.getKey());
                    String vEnc = customEncode(entry.getValue());
                    parts.add(kEnc + "=" + vEnc);
                }
                return String.join("&", parts);
            } else {
                if (url != null && url.contains("?")) {
                    return url.split("\\?", 2)[1];
                }
                return "";
            }
        } else {
            if (data != null) {
                try {
                    if (data instanceof Map || data instanceof List) {
                        return objectMapper.writeValueAsString(data);
                    } else if (data instanceof String) {
                        return data.toString();
                    }
                } catch (Exception e) {
                    logger.error("Failed to serialize data", e);
                }
            }
            return "{}";
        }
    }

    /**
     * 组装请求头
     */
    private Map<String, String> buildHeaders(String method, String urlPath, Map<String, String> params, Object data, String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put("accept", "application/json, text/plain, */*");
        headers.put("accept-language", "zh-CN,zh;q=0.9");
        headers.put("cache-control", "no-cache");
        headers.put("origin", WEB_ORIGIN);
        headers.put("os", config.getOs());
        headers.put("pragma", "no-cache");
        headers.put("referer", WEB_ORIGIN + "/");
        headers.put("user-agent", config.getUserAgent());
        headers.put("x-feature-version", config.getFeatureVersion());
        headers.put("x-api-key", config.getApiKey());

        if (token != null && !token.isEmpty()) {
            headers.put("token", token);
        }

        if (config.getClientIp() != null && !config.getClientIp().isEmpty()) {
            headers.put("x-client-ip", config.getClientIp());
        }

        headers.put("x-uuid", config.getClientUuid());

        long ts = System.currentTimeMillis() + config.getServerTimeOffset();
        String tsStr = String.valueOf(ts);
        headers.put("x-timestamp", tsStr);

        String nonce = UUID.randomUUID().toString();
        headers.put("x-nonce", nonce);

        String fullUrl = BASE_URL + urlPath;
        String a = buildAString(method, params, data, fullUrl);
//        log.info("aaaa: {}", a);
        String sig = sign(a, tsStr, nonce, token);
//        log.info("sig: {}", sig);
        headers.put("x-signature", sig);

        return headers;
    }

    /**
     * 获取登录 UUID 和二维码
     */
    public Map<String, String> getLoginUuid() throws IOException {
        String path = "/user/loginuuid";
        Map<String, String> headers = buildHeaders("GET", path, null, null, null);

        Request request = new Request.Builder()
                .url(BASE_URL + path)
                .headers(Headers.of(headers))
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return objectMapper.readValue(response.body().string(), Map.class);
        }
    }

    /**
     * 获取登录 UUID 和二维码
     */
    public Map<String, String> ping() throws IOException {
        String path = "/ping";
        Map<String, String> headers = buildHeaders("GET", path, null, null, null);

        Request request = new Request.Builder()
                .url(BASE_URL + path)
                .headers(Headers.of(headers))
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return null;
            }
            return objectMapper.readValue(response.body().string(), Map.class);
        }
    }

    /**
     * 轮询扫码状态
     */
    public Map<String, String> getLoginStatus(String uuid) throws IOException {
        String path = "/user/loginstatus";
        Map<String, String> params = new HashMap<>();
        params.put("uuid", uuid);
        Map<String, String> headers = buildHeaders("GET", path, params, null, null);

        HttpUrl url = HttpUrl.parse(BASE_URL + path).newBuilder()
                .addQueryParameter("uuid", uuid)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return objectMapper.readValue(response.body().string(), Map.class);
        }
    }

    /**
     * 获取用户群聊
     */
    public List<UUURegionDTO> getRegions() throws IOException {
        String path = "";
        Map<String, String> params = new LinkedHashMap<>();

        Map<String, String> headers = buildHeaders("GET", path, params, null, null);

        HttpUrl url = HttpUrl.parse(REGION_URL + path).newBuilder()
                .build();

        Request request = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return null;
            }
            return JSON.parseObject(response.body().string(), UUURegionVO.class).getApi_addrs();
        }
    }

    /**
     * 获取用户群聊
     */
    public List<UUURegionDTO> getRegions2() throws IOException {
        String path = "";
        Map<String, String> params = new LinkedHashMap<>();

        Map<String, String> headers = buildHeaders("GET", path, params, null, null);

        HttpUrl url = HttpUrl.parse(REGION_URL_2 + path).newBuilder()
                .build();

        Request request = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return null;
            }
            return JSON.parseObject(response.body().string(), UUURegionVO.class).getApi_addrs();
        }
    }


    public List<UUUGroupDTO> getGroups(String token) throws IOException {
        String path = "/group/page/mine";
        int pageSize = 20;
        int pageIndex = 1;
        List<UUUGroupDTO> allGroups = new ArrayList<>();

        while (true) {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("page_size", String.valueOf(pageSize));
            params.put("page_index", String.valueOf(pageIndex));
            params.put("keyword", "");
            Map<String, String> headers = buildHeaders("GET", path, params, null, token);

            HttpUrl url = HttpUrl.parse(BASE_URL + path).newBuilder()
                    .addQueryParameter("page_size", String.valueOf(pageSize))
                    .addQueryParameter("page_index", String.valueOf(pageIndex))
                    .addQueryParameter("keyword", "")
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .headers(Headers.of(headers))
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                String string = response.body().string();
//                System.out.println("string: {}" + string);
                List<UUUGroupDTO> pageGroups =
                        JSON.parseObject(string, UUUGroupVO.class).getList();

                if (pageGroups == null || pageGroups.isEmpty()) {
                    break;
                }
                allGroups.addAll(pageGroups);
                if (pageGroups.size() < pageSize) {
                    break;
                }

            }
            pageIndex++;
        }

        return allGroups;
    }


    public List<UUUFriendDTO> getFriends(String token) throws IOException {
        String path = "/friend/sync";
        Map<String, String> params = new LinkedHashMap<>();
        params.put("version", "");
        params.put("api_version", 1 + "");
        params.put("limit", "10000");
        Map<String, String> headers = buildHeaders("GET", path, params, null, token);

        HttpUrl url = HttpUrl.parse(BASE_URL + path).newBuilder()
                .addQueryParameter("version", "")
                .addQueryParameter("api_version", "1")
                .addQueryParameter("limit", "10000")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return null;
            }
            String string = response.body().string();
            return JSON.parseArray(string, UUUFriendDTO.class);
//            return objectMapper.readValue(response.body().string(), List.class);
        }
    }

    /**
     * 使用 auth_code 完成登录
     */
    public Map<String, String> loginWithAuthCode(String authCode) throws IOException {
        String path = "/user/login_authcode/" + authCode;
        Map<String, String> headers = buildHeaders("POST", path, null, "{}", null);

        Request request = new Request.Builder()
                .url(BASE_URL + path)
                .headers(Headers.of(headers))
                .post(RequestBody.create("{}", MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return objectMapper.readValue(response.body().string(), Map.class);
        }
    }

    /**
     * 创建零时空间
     */
    public Map<String, String> createStranger(String to_uid, String token) throws IOException {
        // 1. 构造 JSON body
        ObjectNode body = objectMapper.createObjectNode();
        body.put("to_uid", to_uid);
        String jsonBody = body.toString();
        log.info("jsonBody: {}", jsonBody);
        String path = "/user/stranger/create";
        Map<String, String> headers = buildHeaders("POST", path, null, jsonBody, token);
        Request request = new Request.Builder()
                .url(BASE_URL + path)
                .headers(Headers.of(headers))
                .post(RequestBody.create(jsonBody, MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return objectMapper.readValue(response.body().string(), Map.class);
        }
    }

    /**
     * 一键走完整扫码登录流程
     */
    public Map<String, String> runQrLoginFlow(float pollInterval, int timeout) throws IOException, InterruptedException {
        // 1. 获取 uuid
        Map<String, String> uuidInfo = getLoginUuid();
        String uuid = (String) uuidInfo.get("uuid");
        String qrcodeUrl = (String) uuidInfo.get("qrcode");

        logger.info("获取到登录 UUID: {}", uuid);
        printQrConsole(qrcodeUrl);

        long start = System.currentTimeMillis();
        String lastStatus = null;
        String authCode = null;

        // 2. 轮询扫码状态
        while (true) {
            if (System.currentTimeMillis() - start > timeout * 1000L) {
                throw new IOException("扫码登录超时");
            }

            Map<String, String> statusResp = getLoginStatus(uuid);
            String status = (String) statusResp.get("status");
            if (!status.equals(lastStatus)) {
                logger.info("当前扫码状态: {}", status);
                lastStatus = status;
            }

            if ("waitScan".equals(status)) {
                Thread.sleep((long) (pollInterval * 1000));
                continue;
            } else if ("scanned".equals(status)) {
                String uid = (String) statusResp.get("uid");
                if (uid != null) {
                    logger.info("已扫码，UID={}，等待手机确认...", uid);
                }
                Thread.sleep((long) (pollInterval * 1000));
                continue;
            } else if ("authed".equals(status)) {
                authCode = (String) statusResp.get("auth_code");
                logger.info("手机确认登录，auth_code={}", authCode);
                if (authCode == null || authCode.isEmpty()) {
                    throw new RuntimeException("authed 状态未返回 auth_code");
                }
                break;
            } else if ("expired".equals(status)) {
                throw new RuntimeException("二维码已过期，请重新开始登录流程");
            } else {
                logger.warn("未知状态或返回：{}", statusResp);
                Thread.sleep((long) (pollInterval * 1000));
            }
        }

        // 3. 用 auth_code 完成登录
        Map<String, String> loginResp = loginWithAuthCode(authCode);
        logger.info("扫码登录完成");
        return loginResp;
    }

    private void printQrConsole(String url) {
        logger.info("控制台输出二维码，请扫码: {}", url);
        // TODO: 使用 ZXing 生成二维码并打印到控制台
    }

    /**
     * 获取 OSS STS 临时凭证（用于直传/分片上传等）
     * GET /v1/file/sts
     */
    public Map<String, Object> getFileSts(String token) throws IOException {
        String path = "/file/sts";

        // 生成签名头（x-timestamp/x-nonce/x-signature 等）
        Map<String, String> headers = buildHeaders("GET", path, null, null, token);

        // 补充抓包里出现但对签名无影响的头（可选）
        headers.putIfAbsent("x-package", "com.uuutalk.im");

        Request request = new Request.Builder()
                .url(BASE_URL + path)
                .headers(Headers.of(headers))
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String body = response.body() != null ? response.body().string() : "{}";
            Map<String, Object> data = objectMapper.readValue(body, Map.class);

            // 方便调用方判断是否过期：把 ISO8601 的 expiration 解析成时间戳（秒）
            // 示例: 2025-12-16T10:42:36Z
            Object expObj = data.get("expiration");
            if (expObj != null) {
                String exp = String.valueOf(expObj);
                Long expTs = tryParseIsoToEpochSeconds(exp);
                if (expTs != null) {
                    data.put("expiration_ts", expTs);
                }
            }
            return data;
        }
    }

    private Long tryParseIsoToEpochSeconds(String iso) {
        if (iso == null || iso.isEmpty()) return null;

        // 常见情况：以 Z 结尾（UTC）
        try {
            Instant instant = Instant.parse(iso);
            return instant.getEpochSecond();
        } catch (DateTimeParseException ignore) {
            // fallback：带偏移的格式，如 2025-12-16T10:42:36+00:00
        }

        try {
            OffsetDateTime odt = OffsetDateTime.parse(iso);
            return odt.toInstant().getEpochSecond();
        } catch (DateTimeParseException ignore) {
            return null;
        }
    }

    /**
     * 拉取群成员列表（全量 / 增量同步）
     * GET /v1/groups/{group_id}/membersync
     *
     * @param groupId 群 ID（如 a742c01351e64d81be81c63e533be439）
     * @param token   鉴权 token
     * @param version 增量版本号，0 表示全量
     * @param limit   返回成员上限
     */
    public List<UUUGroupMemberDTO> syncGroupMembers(
            String groupId,
            String token,
            Object version,
            int limit
    ) throws IOException {

        String path = "/groups/" + groupId + "/membersync";

        Map<String, String> params = new LinkedHashMap<>();
        params.put("version", String.valueOf(version));
        params.put("limit", String.valueOf(limit));

        Map<String, String> headers = buildHeaders("GET", path, params, null, token);

        HttpUrl url = HttpUrl.parse(BASE_URL + path).newBuilder()
                .addQueryParameter("version", String.valueOf(version))
                .addQueryParameter("limit", String.valueOf(limit))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            logger.info("groups/membersync status={}", response.code());

            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String body = response.body() != null ? response.body().string() : "{}";
            return objectMapper.readValue(
                    body,
                    new com.fasterxml.jackson.core.type.TypeReference<List<UUUGroupMemberDTO>>() {}
            );
        }
    }

    public List<UUUGroupMemberDTO> getUserInfo(
            String groupId,
            String token,
            String uid

    ) throws IOException {

        String path = "/users/" + uid;

        Map<String, String> params = new LinkedHashMap<>();
        params.put("group_no", String.valueOf(groupId));

        Map<String, String> headers = buildHeaders("GET", path, params, null, token);

        HttpUrl url = HttpUrl.parse(BASE_URL + path).newBuilder()
                .addQueryParameter("group_no", String.valueOf(groupId))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            logger.info("getUserInfo: {}", response.code());

            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String body = response.body() != null ? response.body().string() : "{}";
            System.out.println(body);
            return null;
        }
    }



    public static void main(String[] args) throws IOException {
        UuutalkApiClient uuutalkApiClient = new UuutalkApiClient();
//        List<UUUGroupDTO> groups = uuutalkApiClient.getGroups("793a968bbcba415ebb2daadf0edc5abf");
//        System.out.println(groups);
        List<UUUGroupMemberDTO> userInfo = uuutalkApiClient.getUserInfo("88d0888477c54cf9b128cefc0ad8dc71", "c84fcb3f1cea45e49c74953f0e55c345", "0902c3f8c6734355b33dced73300542e");
    }
}
