package cn.keeponline.telegram.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class FixedSizeMap<K, V> extends LinkedHashMap<K, V> {

    private final int maxSize;

    public FixedSizeMap(int maxSize) {
        // true = 按访问顺序（最近访问的放后面）
        // false = 按插入顺序（最早插入的在前）
        super(16, 0.75f, false);
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }

    public static void main(String[] args) {
        Map<String, String> map = new FixedSizeMap<>(100);

        for (int i = 1; i <= 120; i++) {
            map.put("msg" + i, "content" + i);
        }

        System.out.println(map.size()); // 100
        System.out.println(map.keySet()); // msg21 ~ msg120
    }
}