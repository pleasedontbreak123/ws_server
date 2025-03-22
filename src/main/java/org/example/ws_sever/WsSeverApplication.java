package org.example.ws_sever;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * WebSocket服务器应用程序入口类
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {
    "org.example.ws_sever.config", 
    "org.example.ws_sever.utils",
    "org.example.ws_sever.controller",  // 添加 controller 包
    "org.example.ws_sever.service"      // 添加 service 包以支持其他功能
})
public class WsSeverApplication {

    public static void main(String[] args) {
        SpringApplication.run(WsSeverApplication.class, args);
    }

}
