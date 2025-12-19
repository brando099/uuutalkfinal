
package cn.keeponline.telegram.test;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

import java.util.HashMap;
import java.util.Map;

public class UneedUploadFile {
    public static void main(String[] args) {
        String token = "ab9fbbcdc1c1be76493acdb41463ab29";

        // === 构造 URL ===
        String url = "https://ups-qn-bj.uneedx.com/v2/upload/file" +
                "?business_id=1179466444509814784" +
                "&business_type=2" +
                "&encrypt=false" +
                "&expire=1762158519872" +
                "&file_md5=0fdd88ae5ff994ecd825594ad033b9fc" +
                "&file_name=f17815a0-a190-48c2-b61d-fbf1018a9618.png" +
                "&file_size=9834" +
                "&finish_flag=0" +
                "&modify_time=0" +
                "&not_notify=false" +
                "&object_key=" +
                "&region=" +
                "&source=0" +
                "&tags=" +
                "&uid=964481395340619776" +
                "&vip_archived=false" +
                "&signature=N%2FDJMPSZ3D2OD8XUNK1WG01V7%2B4NR89701WWATXYAJG%3D" +
                "&token=" + token;

        // === POST 数据 ===
        String reqData = "AAABsAABAAEAAAAAAAABoJHleQwQNpTDiYl7D2vvww9IC%2FeDEoDfioScd%2FZOoYkzJEiVfDlw8osJh%2FTJE1AZ0wBS9%2F%2FENBLuG7O2%2FHDVKxYdN%2BGbWUwzRHQRnar80lEynd9t%2BoF3l0yPXRuF1o7KDZzeNAQNudIXBtjjR0Us006Q3MEcPEXXRSgCr3wNUrhtRyod0n3JyNtRisp9iqqNVoNPxrdcoNqZnUyBuSWFc%2BISr35yHmNkMmjKL2ViAtdScpyMZTdslAEz0d8cYGZnt%2BeF5UC1YUKGz%2BFpVObLFlN26JJVd7da6tpKOWr23i0GyKMfFWciHa%2FDcH3%2F%2BzXx7pxr9X7w0%2FS3X6tHrnHmWVuY%2B2FsV21ow0muWU4OIPCjHycEADNkMm4UcOtWlgsWHJI8ki3LYe23cZO1S85TenYJ0u13sr7BrnftWWA94Hq2gXn2KL6kIcv0EK6PDYmOAOBQOBVjte0OrODCmVyA6sp98%2FTdVPwOvq2aKHG6yVzyU0epXODBt9e1WpiucdfByZgcFEek17llq27EG49oXxhIuIN2ZBDilvU%2B1W3%2FjP5g";

        Map<String, Object> form = new HashMap<>();
        form.put("req", reqData);

        // === 发送请求 ===
        HttpResponse response = HttpRequest.post(url)
                .header("authority", "ups-qn-bj.uneedx.com")
                .header("accept", "application/json, text/plain, */*")
                .header("encrypt-type", "1")
                .header("user-agent", "UneedGroup/3.8.0 BROWSER/11.5.0 (darwin-x64;Release;Build-0-9-0;7c435ebea5b826bae7924c9ec8cd70d515b528a5a2029c93d7109343f05de934)")
                .header("content-type", "application/x-www-form-urlencoded")
                .header("sec-fetch-site", "cross-site")
                .header("sec-fetch-mode", "cors")
                .header("sec-fetch-dest", "empty")
                .header("accept-language", "zh-CN")
                .form(form)
                .timeout(15000)
                .execute();

        byte[] encryptedBody = response.bodyBytes();

        // === 输出结果 ===


        // === 若返回内容需要解密，可调用 te() ===
        try {
            String decrypted = (String)SendMessage.te(encryptedBody, true);
            System.out.println("\n✅ 解密后内容：");
            System.out.println(decrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}