package org.example.ws_sever.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.ws_sever.service.MessageBroadcastService;
import org.example.ws_sever.service.SocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

/**
 * WebSocket消息处理器
 * 负责处理WebSocket连接的生命周期管理
 */
@Slf4j
@Component
public class WebSocketHandler extends AbstractWebSocketHandler {

    @Autowired
    private MessageBroadcastService messageBroadcastService;
    
    @Autowired
    private SocketService socketService;

    /**
     * 当WebSocket连接建立成功后调用
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            Integer userId = (Integer) session.getAttributes().get("userId");
            if (userId != null) {
                messageBroadcastService.registerSession(userId, session);
                log.info("用户 [{}] 建立连接", userId);
            } else {
                log.warn("连接建立失败：userId为空");
                session.close();
            }
        } catch (Exception e) {
            log.error("连接建立异常", e);
            throw e;
        }
    }

    /**
     * 处理接收到的WebSocket消息
     */
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            Integer userId = (Integer) session.getAttributes().get("userId");
            if (userId != null) {
                String payload = message.getPayload().toString();
                log.info("收到用户 [{}] 的消息：{}", userId, payload);
                socketService.handleMessage(payload);
            }
        } catch (Exception e) {
            log.error("消息处理异常", e);
            throw e;
        }
    }

    /**
     * 处理WebSocket传输过程中的错误
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("传输错误，userId: " + session.getAttributes().get("userId"), exception);
        try {
            if (session.isOpen()) {
                session.close();
            }
        } finally {
            Integer userId = (Integer) session.getAttributes().get("userId");
            if (userId != null) {
                messageBroadcastService.removeSession(userId);
                socketService.removeUserLcoation(userId);
            }
        }
        super.handleTransportError(session, exception);
    }

    /**
     * 当WebSocket连接关闭后调用
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        try {
            Integer userId = (Integer) session.getAttributes().get("userId");
            if (userId != null) {
                messageBroadcastService.removeSession(userId);
                socketService.removeUserLcoation(userId);
                log.info("用户 [{}] 关闭连接，状态码：{}，原因：{}", 
                    userId, status.getCode(), status.getReason());
            }
        } finally {
            super.afterConnectionClosed(session, status);
        }
    }
}
