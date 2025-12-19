package cn.keeponline.telegram.test;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

public class UneedRequest {
    public static void main(String[] args) {
        String url = "https://api.uneed.com/v2/profile/view/1000000000?token=13d5d7668137e3d8f7fb5c55ba905f88";

        // 直接使用 curl 中的 --data-raw（已 URL-encoded）
        String payload = "req=AAAAcAABAAEAAAAAAAAAYJHleQwQNpTDiYl7D2vvww%2BYb5XJQaeQhZ0wcc3exgUuwXCs85tJa0OXWqjo12n87gGaVGzPG%2BXEPypev24eSStBLxris2CNFhXqDHCiVJz6zoxjSE3qXJgy977eMcHCZQ%3D%3D";

        HttpResponse response = HttpRequest.post(url)
            .header("authority", "api.uneed.com")
            .header("accept", "application/json, text/plain, */*")
            .header("accept-language", "zh-CN")
            .header("content-type", "application/x-www-form-urlencoded")
            .header("encrypt-type", "1")
            .header("referer", "https://vod.uneed.com")
            .header("sec-fetch-dest", "empty")
            .header("sec-fetch-mode", "cors")
            .header("sec-fetch-site", "cross-site")
            .header("user-agent", "UneedGroup/3.8.0 BROWSER/11.5.0 (darwin-x64;Release;Build-0-9-0;7c435ebea5b826bae7924c9ec8cd70d515b528a5a2029c93d7109343f05de934)")
            .body(payload)          // 直接原样发送
            .timeout(60_000)        // 超时可调
            .execute();

        System.out.println("Status: " + response.getStatus());
        System.out.println("Body: " + response.body());
    }
}