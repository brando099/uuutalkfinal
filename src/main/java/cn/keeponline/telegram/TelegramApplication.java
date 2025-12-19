package cn.keeponline.telegram;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("cn.keeponline.telegram.mapper")
@EnableAsync
@Slf4j
@EnableScheduling
public class TelegramApplication {
    public static void main(String[] args) {
        SpringApplication.run(TelegramApplication.class, args);
    }
}






