package cn.keeponline.telegram.controller;

import lombok.Data;

@Data
public class TestUpdate2 {
    private String id;
    private String name;
    public static ThreadLocal<String> threadLocal = new ThreadLocal<>();

    public static void main(String[] args) {
        threadLocal.set("1");
        test();
        threadLocal.remove();
    }

    public static void test() {
        String s = threadLocal.get();
        System.out.println(s);
    }
}
