package org.example.ws_sever.config;

import org.example.ws_sever.controller.WebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

import javax.annotation.Resource;

/**
 * WebSocket配置类
 * 
 * @Configuration: 标识这是一个配置类
 * @EnableWebSocket: 启用WebSocket功能
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

   /**
    * WebSocket消息处理器
    */
   @Resource
   WebSocketHandler webSocketHandler;

   /**
    * WebSocket连接拦截器
    */
   @Resource
   WsInterceptor wsInterceptor;

    /**
     * 注册WebSocket处理器
     * 
     * @param registry WebSocket处理器注册表
     * 配置WebSocket的端点为/ws
     * 添加自定义的拦截器
     * 允许所有来源的跨域请求
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws")
                .addInterceptors(wsInterceptor)
                .setAllowedOrigins("*"); // 允许所有跨域请求
    }
}
