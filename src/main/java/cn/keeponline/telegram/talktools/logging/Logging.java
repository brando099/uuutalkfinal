package cn.keeponline.telegram.talktools.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 轻量日志封装，统一格式、级别与落盘路径
 */
public class Logging {
    public static final String DEFAULT_FORMAT = "[%d{yyyy-MM-dd HH:mm:ss}] %level %logger: %msg%n";
    public static final Path DEFAULT_LOG_DIR = Paths.get("logs");
    public static final Path DEFAULT_LOG_FILE = DEFAULT_LOG_DIR.resolve("talk_tools.log");

    /**
     * 初始化全局日志配置
     */
    public static void setupLogging() {
        String level = System.getenv("TALK_TOOLS_LOG_LEVEL");
        if (level == null || level.isEmpty()) {
            level = "DEBUG";
        }

        // Logback 配置通过 logback.xml 文件管理
        // 这里只做基本设置
    }

    /**
     * 获取模块级 logger
     */
    public static Logger getLogger(String name) {
        return LoggerFactory.getLogger(name);
    }

    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
}

