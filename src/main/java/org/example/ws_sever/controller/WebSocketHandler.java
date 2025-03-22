package org.example.ws_sever.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.ws_sever.entity.po.SessionBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket消息处理器
 * 负责处理WebSocket连接的建立、消息收发、错误处理和连接关闭等操作
 * 
 * @Slf4j: Lombok注解，自动生成日志对象
 * @Component: 将该类标记为Spring组件
 */
@Slf4j
@Component
public class WebSocketHandler extends AbstractWebSocketHandler {

    /**
     * 存储所有WebSocket会话信息的Map
     * key: 用户ID
     * value: 会话信息
     * 使用ConcurrentHashMap保证线程安全
     */
    private final Map<Integer, SessionBean> sessionBeanMap = new ConcurrentHashMap<>();

    /**
     * 心跳超时时间（毫秒）
     */
    private static final long HEARTBEAT_TIMEOUT = 60000;

    /**
     * 当WebSocket连接建立成功后调用
     * 
     * @param session WebSocket会话对象
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            Integer userId = (Integer) session.getAttributes().get("userId");
            if (userId == null) {
                log.warn("连接建立失败：userId为空");
                session.close();
                return;
            }
            
            // 检查是否存在同一用户的其他会话
            SessionBean existingSession = sessionBeanMap.get(userId);
            if (existingSession != null) {
                log.warn("用户 [{}] 已有活跃连接，关闭旧连接", userId);
                try {
                    existingSession.getWebSocketSession().close();
                } catch (IOException e) {
                    log.error("关闭旧连接失败", e);
                }
            }
            
            SessionBean sessionBean = new SessionBean(session, userId);
            sessionBean.setLastHeartbeatTime(System.currentTimeMillis());
            sessionBeanMap.put(userId, sessionBean);
            
            log.info("用户 [{}] 建立连接，当前在线数量：{}", userId, sessionBeanMap.size());
            super.afterConnectionEstablished(session);
        } catch (Exception e) {
            log.error("连接建立异常", e);
            throw e;
        }
    }

    /**
     * 处理接收到的WebSocket消息
     * 
     * @param session WebSocket会话对象
     * @param message 接收到的消息
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            Integer userId = (Integer) session.getAttributes().get("userId");
            SessionBean sessionBean = sessionBeanMap.get(userId);
            if (sessionBean == null) {
                log.warn("未找到会话信息，userId: {}", userId);
                return;
            }
            
            // 更新最后心跳时间
            sessionBean.setLastHeartbeatTime(System.currentTimeMillis());
            
            String payload = message.getPayload().toString();
            log.info("收到用户 [{}] 消息: {}", userId, payload);
            
            super.handleMessage(session, message);
        } catch (Exception e) {
            log.error("消息处理异常", e);
            throw e;
        }
    }

    /**
     * 处理WebSocket传输过程中的错误
     * 
     * @param session WebSocket会话对象
     * @param exception 发生的异常
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("传输错误，userId: " + session.getAttributes().get("userId"), exception);
        try {
            if (session.isOpen()) {
                session.close();
            }
        } finally {
            removeSession((Integer) session.getAttributes().get("userId"));
        }
        super.handleTransportError(session, exception);
    }

    /**
     * 当WebSocket连接关闭后调用
     * 
     * @param session WebSocket会话对象
     * @param status 关闭状态
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        try {
            Integer userId = (Integer) session.getAttributes().get("userId");
            if (userId != null) {
                log.info("用户 [{}] 关闭连接，状态码：{}，原因：{}", 
                    userId, status.getCode(), status.getReason());
                removeSession(userId);
            }
        } finally {
            super.afterConnectionClosed(session, status);
        }
    }

    /**
     * 定时发送心跳消息并清理超时连接
     */
    @Scheduled(fixedRate = 1000)
    public void heartbeat() {
        long currentTime = System.currentTimeMillis();
        sessionBeanMap.forEach((userId, sessionBean) -> {
            try {
                WebSocketSession session = sessionBean.getWebSocketSession();
                if (session != null && session.isOpen()) {
                    // 检查是否超时
                    if (currentTime - sessionBean.getLastHeartbeatTime() > HEARTBEAT_TIMEOUT) {
                        log.warn("用户 [{}] 心跳超时，关闭连接", userId);
                        session.close();
                        removeSession(userId);
                    } else {
                        session.sendMessage(new TextMessage("heartbeat"));
                    }
                } else {
                    removeSession(userId);
                }
            } catch (IOException e) {
                log.error("发送心跳消息异常，userId: " + userId, e);
                removeSession(userId);
            }
        });
    }

    /**
     * 安全地移除会话
     * 
     * @param userId 用户ID
     */
    private void removeSession(Integer userId) {
        if (userId != null) {
            SessionBean removed = sessionBeanMap.remove(userId);
            if (removed != null) {
                log.info("移除用户 [{}] 的会话，当前在线数量：{}", userId, sessionBeanMap.size());
            }
        }
    }

    /**
     * 向指定用户发送消息
     * 
     * @param userId 目标用户ID
     * @param message 要发送的消息
     * @return 是否发送成功
     */
    public boolean sendMessageToUser(Integer userId, String message) {
        SessionBean sessionBean = sessionBeanMap.get(userId);
        if (sessionBean != null && sessionBean.getWebSocketSession().isOpen()) {
            try {
                sessionBean.getWebSocketSession().sendMessage(new TextMessage(message));
                return true;
            } catch (IOException e) {
                log.error("发送消息给用户 [{}] 失败", userId, e);
                removeSession(userId);
            }
        }
        return false;
    }
}
