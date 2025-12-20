package cn.keeponline.telegram.talktools.services;

/**
 * UUTalk API 配置
 */
public class UuutalkConfig {
    private String apiKey = "1";
    private String featureVersion = "1.3.15";
    private String clientUuid;
    private String clientIp = "103.36.25.141";
    private String os = "WEB";
    private String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/142.0.0.0 Safari/537.36";
    private int serverTimeOffset = 240;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getFeatureVersion() {
        return featureVersion;
    }

    public void setFeatureVersion(String featureVersion) {
        this.featureVersion = featureVersion;
    }

    public String getClientUuid() {
        return clientUuid;
    }

    public void setClientUuid(String clientUuid) {
        this.clientUuid = clientUuid;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public int getServerTimeOffset() {
        return serverTimeOffset;
    }

    public void setServerTimeOffset(int serverTimeOffset) {
        this.serverTimeOffset = serverTimeOffset;
    }
}

