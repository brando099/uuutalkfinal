package cn.keeponline.telegram.test;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import java.util.HashMap;
import java.util.Map;

public class UneedFileRoute {

    public static void main(String[] args) {
        String token = "ab9fbbcdc1c1be76493acdb41463ab29";
        String url = "https://api.uneed.com/v2/file/route/964481395340619776?token=" + token;

        // POST 数据
//         {"session_meta":{"uid":"964481395340619776","device_sign":"78906dfcb80a41cff8244d217cd4a617"},"params":{"uid":"964481395340619776","business_type":2,"upload_flag":1,"business_id":"1179466444509814784","content_type":"image/png","basic_info":{"modify_time":1762157618686,"file_name":"f17815a0-a190-48c2-b61d-fbf1018a9618.png","file_size":9834},"file_md5":"0fdd88ae5ff994ecd825594ad033b9fc","oss_direct":true}}
        String reqData = "AAABsAABAAEAAAAAAAABoJHleQwQNpTDiYl7D2vvww9IC/eDEoDfioScd/ZOoYkzJEiVfDlw8osJh/TJE1AZ0wBS9//ENBLuG7O2/HDVKxYdN+GbWUwzRHQRnar80lEynd9t+oF3l0yPXRuF1o7KDZzeNAQNudIXBtjjR0Us006Q3MEcPEXXRSgCr3wNUrhtRyod0n3JyNtRisp9iqqNVoNPxrdcoNqZnUyBuSWFc+ISr35yHmNkMmjKL2ViAtdScpyMZTdslAEz0d8cYGZnt+eF5UC1YUKGz+FpVObLFlN26JJVd7da6tpKOWr23i0GyKMfFWciHa/DcH3/+zXx7pxr9X7w0/S3X6tHrnHmWVtZQ40btfAxMTDhyfVedDD9o071G/NVZCspdzeS2U6DVKzO1Jz0/UKFtBZfEUDN/9SPr8OPigJOKhnklBwuUBDoVVsNNtXO6ZMMaCBi8S6tdW1UJUrzOFflRTgSHNZB34D3scUdM6jNqGKYTBhq+lfnMFZqrXuq6Q+7eB3UYcaxUpE5vf3sUR3V7FG+fUyq+zA78b4rwUM52+WzKA+6RIYH";

        Map<String, Object> form = new HashMap<>();
        form.put("req", reqData);

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
                .form(form)
                .timeout(15000)
                .execute();

        byte[] bytes = response.bodyBytes();


        // 假设你项目里有一个 AES 解密方法 te()
        try {
//            String decrypted = (String) SendMessage.te(result.getBytes(), true); // 调用你的解密方法
            System.out.println("✅ 解密后的内容：");
//            System.out.println(decrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}