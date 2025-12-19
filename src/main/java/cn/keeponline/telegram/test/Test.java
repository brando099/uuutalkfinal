package cn.keeponline.telegram.test;

import cn.hutool.core.net.url.UrlQuery;
import cn.keeponline.telegram.dto.FriendDTO;
import com.alibaba.druid.sql.visitor.functions.Char;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;

public class Test {
    public static void main(String[] args) throws Exception {
//        SendMessage.DecryptResult de = SendMessage.de(Base64.getDecoder().decode("AAABsAABAAEAAAAAAAABoJHleQwQNpTDiYl7D2vvww9IC/eDEoDfioScd/ZOoYkzJEiVfDlw8osJh/TJE1AZ0wBS9//ENBLuG7O2/HDVKxYdN+GbWUwzRHQRnar80lEynd9t+oF3l0yPXRuF1o7KDZzeNAQNudIXBtjjR0Us006Q3MEcPEXXRSgCr3wNUrhtRyod0n3JyNtRisp9iqqNVoNPxrdcoNqZnUyBuSWFc+ISr35yHmNkMmjKL2ViAtdScpyMZTdslAEz0d8cYGZnt+eF5UC1YUKGz+FpVObLFlN26JJVd7da6tpKOWr23i0GyKMfFWciHa/DcH3/+zXx7pxr9X7w0/S3X6tHrnHmWVvS65q6xeJldzYtlUtwZ9Ji+ViKn8HVxM+5pwvNDoLTZZTm+1T26OEKMVrJfM5eNyv2H3PBCL0uGHxyPKjhLZwQlAtdG05T7LLh68H9ThaaHe0W5on8zX+mnwbw5oHIZehVShWh7P4m8KlD0l1dyERI3jgR+khAxkgXuSLy+16ltccw355RdtYommleygveJ4mEOXNa6meE8y6SwcCnrJiy"));
//
//        String s = new String(de.data);
//        System.out.println(s);
//
//        Long upload_time = 1762165303L;
//        String s1 = upload_time * 1000 + 1000 * 60 * 15 + "";
//        Date date = new Date(Long.parseLong(s1));
//        System.out.println(date);


//        String refreshURL = """
//                https://ups-qn-bj.uneedx.com/v2/upload/urls?bucket=uneed-file-private\\u0026business_id=1179466444509814784\\u0026business_type=2\\u0026content_type=image%2Fpng\\u0026encrypt=false\\u0026encrypt_iv=yby9qmVnSrvg5dCxIod0PncYalBz4Z7oOFf4Eca3PEnh7bCLsen7p8ygIzAq%2B3F8Tyfa2ocQI7PNnX9uRm1GOmrnzLWfNAE%2Bv9BYCmBylp6nK%2BR7G7OSBs6ySbgFoU%2B67GOzEt0dWYzw4zEdvuNwFNDBsNjbY3D7UNX58NZp2pj7zDE1rbChg0XPBkOcpl%2B1IqZkVED7y9LUBtKe619F%2BUh65Yaag21yFEYjYdh81I07VMYEINigNMFVBO91ZiMEkw%2BnyYId6KnCvm51EfpFHJMF5SCUUby82SqT%2FHYtg9YzRxkF4chI80cCqGNUrylEzNyg4RAGr3VQxuxGiDZe1A%3D%3D\\u0026encrypt_key=MFxfxJJ7rDNVCXX22M6%2BCop4oCBX6LQP00dnhLrTH5%2Fp%2FFK4nqrHvRkeotlE3FiBYJQvQZ3F0RjbKWZaawPLsrYpWRqmziZD0SJhSLvL5K9VOE%2FquM7zeuLx1IXSHbvpV8rvReDwPCmDDyyAWYtsxdloKUZ%2B%2FJutSww0Fw%2Ft%2FSKkT9qXXkVLwGAm8PqkMl%2BUcaC5t3%2FQuh6MKL43C9Ykqavl%2B4HULtbvCTocUnBxNA6DXqwitnAIwffbKCRmCqWiwgnWm8S5p%2FCmEbblOYS4ZVxaiBzs8G5i%2BgzLZIRqVDqrMw4g1nhKiRGTcMzk9qwG0KCQTIn0ug04w72bnCEjAQ%3D%3D\\u0026expire=1762221941392\\u0026file_md5=52526920730c406e6f87359cc781cafd\\u0026file_name=de962547-818d-476c-9ce5-bc2ac8aa2fd0.png\\u0026file_size=2207\\u0026key_version=v1\\u0026objectkey=chat%2F52526920730c406e6f87359cc781cafd%2Forigin\\u0026oss_direct=true\\u0026region=z1\\u0026uid=964481395340619776\\u0026upload_time=1762221041\\u0026vip_archived=false\\u0026signature=9863UIYSCMPIIFMUWCTY%2FGQ30WY89NAR6ZSVLIO5PS8%3D&token=c8906e5c35262d607cc303d6eebc474e
//                """;
//        refreshURL = refreshURL.replaceAll("u0026", "&").replaceAll("\\\\", "");
//        System.out.println(refreshURL);
//
//        String expire = UrlQuery.of(refreshURL, Charset.defaultCharset()).get("expire").toString();
//        String signature = UrlQuery.of(refreshURL, null).get("signature").toString();
//        System.out.println(expire);
//        System.out.println(signature);

        List<FriendDTO> friendList = SendMessage.getFriendList("964481395340619776", "5cc0e1e1bb6b5db9febeb00e3a059385");


//        JSONObject officialList = SendMessage.getOfficialList("964481395340619776", "5cc0e1e1bb6b5db9febeb00e3a059385");
//        System.out.println(JSON.toJSONString(officialList));

    }
}
