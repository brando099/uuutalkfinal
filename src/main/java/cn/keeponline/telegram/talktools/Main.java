package cn.keeponline.telegram.talktools;

import cn.keeponline.telegram.talktools.config.UUTalkGlobalConfig;
import cn.keeponline.telegram.talktools.logging.Logging;
import cn.keeponline.telegram.talktools.services.UuutalkApiClient;
import cn.keeponline.telegram.talktools.uutalk.UUTalkClient;
import com.alibaba.fastjson2.JSON;

import org.slf4j.Logger;

import java.util.Map;

/**
 * 主入口类
 */
public class Main {
    private static final Logger logger = Logging.getLogger(Main.class);
    private static final UUTalkGlobalConfig GLOBAL_CONFIG = new UUTalkGlobalConfig();

    public static void main(String[] args) {
        Logging.setupLogging();

        try {
            // 1. 扫码登录
//            UuutalkApiClient apiClient = new UuutalkApiClient();
////            Map<String, Object> groups = apiClient.getGroups();
////            logger.info("groups: {}", JSON.toJSONString(groups));
//            logger.info("开始扫码登录流程...");
//            Map<String, String> loginResp = apiClient.runQrLoginFlow(2.0f, 180);
//            logger.info("登录成功, loginResp: {}", loginResp);

//            String uid = (String) loginResp.get("uid");
//            String token = (String) loginResp.get("token");
//
//            if (uid == null || token == null) {
//                throw new RuntimeException("登录返回中缺少 uid/token: " + loginResp);
//            }
            // {app_id=, avatar=users/59ae5a30bcf34c349b135b9c8fccac86/avatar, im_pub_key=, name=风趣紫罗兰, short_no=AP2DH6RRYO, token=04a24819bce74ad894a416c0177bd67e, uid=59ae5a30bcf34c349b135b9c8fccac86, username=008617391836647}

            // 更新全局配置
//            GLOBAL_CONFIG.uid = uid;
//            GLOBAL_CONFIG.token = token;
//            GLOBAL_CONFIG.username = (String) loginResp.get("username");
//            GLOBAL_CONFIG.avatar = (String) loginResp.get("avatar");
//            GLOBAL_CONFIG.shortNo = (String) loginResp.get("short_no");

            String uid = "59ae5a30bcf34c349b135b9c8fccac86";
            String token = "04a24819bce74ad894a416c0177bd67e";
            GLOBAL_CONFIG.uid = "59ae5a30bcf34c349b135b9c8fccac86";
            GLOBAL_CONFIG.token = "04a24819bce74ad894a416c0177bd67e";
//            GLOBAL_CONFIG.username = "008617391836647";
//            GLOBAL_CONFIG.avatar = "users/59ae5a30bcf34c349b135b9c8fccac86/avatar";
//            GLOBAL_CONFIG.shortNo = "AP2DH6RRYO";

//            logger.info("扫码登录成功：uid={}, token={}", uid, token);

//            Map<String, Object> groups = apiClient.getGroups(token);
//            logger.info("groups: {}", JSON.toJSONString(groups));

            // 2. 启动 WebSocket 客户端
            System.out.println("准备连接 IM WebSocket 服务...");
//            UUTalkClient.runWsClient(uid, token);

        } catch (Exception e) {
            logger.error("发生错误", e);
            System.exit(1);
        }
    }
}

