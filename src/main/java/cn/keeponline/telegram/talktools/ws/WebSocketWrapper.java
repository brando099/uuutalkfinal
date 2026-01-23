package cn.keeponline.telegram.talktools.ws;

import cn.keeponline.telegram.talktools.logging.Logging;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * WebSocket 包装类
 * 使用 OkHttp WebSocket
 */
@Data
@Slf4j
public class WebSocketWrapper {
    private static final Logger logger = Logging.getLogger(WebSocketWrapper.class);

    private final String url;
    private final String origin;
    private final Headers headers;
    private OkHttpClient client;
    private WebSocket webSocket;
    private boolean destroy = false;
    private boolean reconnect = true;
    private int reconnectInterval = 5; // 秒
    private int pingInterval = 25; // 秒
    private String uid;

    private Runnable onOpenCallback;
    private MessageCallback onMessageCallback;
    private CloseCallback onCloseCallback;
    private ErrorCallback onErrorCallback;

    public WebSocketWrapper(String url, String origin, Headers headers, String uid) {
        this.url = url;
        this.origin = origin;
        this.headers = headers;
        this.uid = uid;
        this.client = new OkHttpClient.Builder()
                .pingInterval(pingInterval, TimeUnit.SECONDS)
                .build();
    }

    public void connect() {
        Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                logger.info("WebSocket open");
                if (!destroy && onOpenCallback != null) {
                    onOpenCallback.run();
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, okio.ByteString bytes) {
                if (!destroy && onMessageCallback != null) {
                    onMessageCallback.onMessage(bytes.toByteArray());
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                // 文本消息，通常不会用到
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                logger.info("WebSocket closing: {} {}", code, reason);
                if (!destroy && onCloseCallback != null) {
                    onCloseCallback.onClose(code, reason);
                }
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                logger.info("WebSocket closed: {} {}", code, reason);
                if (!destroy && onCloseCallback != null) {
                    onCloseCallback.onClose(code, reason);
                }
//                if (!destroy && reconnect) {
//                    scheduleReconnect();
//                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                logger.error("WebSocket error", t);
                if (!destroy && onCloseCallback != null) {
                    onCloseCallback.onClose(1001, "代码错误");
                }
//                if (!destroy && onErrorCallback != null) {
//                    onErrorCallback.onError(t);
//                }
//                if (!destroy && reconnect) {
//                    scheduleReconnect();
//                }
            }
        });
    }

    private void scheduleReconnect() {
        new Thread(() -> {
            try {
                Thread.sleep(reconnectInterval * 1000L);
                if (!destroy && reconnect) {
                    logger.info("尝试重连 WebSocket: {}", url);
                    connect();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public void onOpen(Runnable callback) {
        this.onOpenCallback = callback;
    }

    public void onMessage(MessageCallback callback) {
        this.onMessageCallback = callback;
    }

    public void onClose(CloseCallback callback) {
        this.onCloseCallback = callback;
    }

    public void onError(ErrorCallback callback) {
        this.onErrorCallback = callback;
    }

    public boolean send(byte[] data) {
        if (webSocket != null) {
            return webSocket.send(okio.ByteString.of(data));
        } else {
            logger.warn("ws 尚未连接，无法发送");
            return false;
        }
    }

    public void close() {
        log.info("进入到了close，将destroy改为了true");
        destroy = true;
        reconnect = false;
        if (webSocket != null) {
            webSocket.close(1000, "Normal closure");
        }
    }

    public interface MessageCallback {
        void onMessage(byte[] data);
    }

    public interface CloseCallback {
        void onClose(int code, String reason);
    }

    public interface ErrorCallback {
        void onError(Throwable error);
    }

    public void setReconnect(boolean reconnect) {
        this.reconnect = reconnect;
    }

    public void setReconnectInterval(int reconnectInterval) {
        this.reconnectInterval = reconnectInterval;
    }

    public void setPingInterval(int pingInterval) {
        this.pingInterval = pingInterval;
    }
}

