package org.example.ws_sever.config;

import lombok.extern.slf4j.Slf4j;
import org.example.ws_sever.utils.JwtUtils;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import javax.annotation.Resource;
import java.util.Map;

/**
 * WebSocket连接拦截器
 * 用于在WebSocket握手前后进行处理，包括JWT验证
 * 
 * @Component: 将该类标记为Spring组件
 * @Slf4j: Lombok注解，自动生成日志对象
 */
@Component
@Slf4j
public class WsInterceptor extends HttpSessionHandshakeInterceptor {

    @Resource
    private JwtUtils jwtUtils;

    /**
     * 在WebSocket握手之前调用
     * 
     * @param request WebSocket请求对象
     * @param response WebSocket响应对象
     * @param wsHandler WebSocket处理器
     * @param attributes 握手属性
     * @return 如果返回true则继续握手，false则中断握手
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            // 从URL参数中获取token
            String token = servletRequest.getServletRequest().getParameter("token");
            
            if (!StringUtils.hasText(token)) {
                log.warn("WebSocket连接验证失败：token为空");
                return false;
            }

            // 验证token
            if (!jwtUtils.validateToken(token)) {
                log.warn("WebSocket连接验证失败：token无效");
                return false;
            }

            // 获取用户ID并存入attributes
            Integer userId = JwtUtils.getUserIdFromToken(token);
            if (userId == null) {
                log.warn("WebSocket连接验证失败：无法获取用户ID");
                return false;
            }

            attributes.put("userId", userId);
            log.info("用户 [{}] 开始握手", userId);
            return super.beforeHandshake(request, response, wsHandler, attributes);
        }
        
        return false;
    }

    /**
     * 在WebSocket握手完成后调用
     * 
     * @param request WebSocket请求对象
     * @param response WebSocket响应对象
     * @param wsHandler WebSocket处理器
     * @param ex 握手过程中发生的异常，如果没有异常则为null
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception ex) {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            String token = servletRequest.getServletRequest().getParameter("token");
            Integer userId = jwtUtils.getUserIdFromToken(token);
            if (userId != null) {
                log.info("用户 [{}] 握手完成", userId);
            }
        }
        super.afterHandshake(request, response, wsHandler, ex);
    }

}
