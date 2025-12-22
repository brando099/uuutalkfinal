package cn.keeponline.telegram.talktools.cache;

import cn.keeponline.telegram.talktools.ws.ShareManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalCache {

    public static Map<String, ShareManager> shareMap = new ConcurrentHashMap<>();
}
