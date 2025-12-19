package cn.keeponline.telegram.test;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.ByteString;

@Slf4j
public class YWebSocket {
    private static final OkHttpClient client = new OkHttpClient();
    private static WebSocket webSocket;


    public static WebSocket connectWebSocket(String url, String uid, String accessToken) {
//        if (isWebSocketConnected()) {
//            System.out.println("✅ WebSocket 已经连接");
//            return;
//        }
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Origin", "https://wstool.jackxiang.com")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.9")
                .addHeader("Pragma", "no-cache")
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36")
                .addHeader("Sec-WebSocket-Version", "13")
                .build();

        String authMessage = """
                {"header":{"sm":1,"ver":10,"uid":"{uid}","cmdtype":"g.auth"},"body":{"uid":"{uid}","gameid":"nimo-web","access_token":"{access_token}","ua":"UneedGroup/3.8.0 BROWSER/8.5.5 (win32-x64;Release;Build-0-9-0)","timezone":28800,"deviceid":"Browser"}}
                """
                .replace("{uid}", uid)
                .replace("{access_token}", accessToken);
        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                setWebSocket(ws);
                System.out.println("✅ 已连接到服务器");
                ws.send(authMessage);
                System.out.println("📤 已发送: " + authMessage);
            }
            @Override
            public void onMessage(okhttp3.WebSocket ws, String text) {
                System.out.println("📩 收到消息: " + text);
            }
            @Override
            public void onMessage(okhttp3.WebSocket ws, ByteString bytes) {
                System.out.println("📦 收到二进制消息: " + bytes.hex());
            }
            @Override
            public void onClosing(okhttp3.WebSocket ws, int code, String reason) {
                System.out.println("🚪 连接关闭: " + reason);
                ws.close(1000, null);
            }
            @Override
            public void onFailure(okhttp3.WebSocket ws, Throwable t, Response response) {
                System.err.println("❌ 连接失败: " + t.getMessage());
            }
        };
        // 建立连接
        return client.newWebSocket(request, listener);
    }

    // 检查 WebSocket 是否已连接
    public static synchronized boolean isWebSocketConnected() {
        return webSocket != null;
    }

    public static okhttp3.WebSocket getWebSocket() {
        return webSocket;
    }

    public static void setWebSocket(okhttp3.WebSocket webSocket) {
        YWebSocket.webSocket = webSocket;
    }
}
