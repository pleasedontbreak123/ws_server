package org.example.ws_sever.entity.po;

import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

/**
 * WebSocket会话信息实体类
 */
@Data
public class SessionBean {
    /**
     * WebSocket会话对象
     */
    private WebSocketSession webSocketSession;
    
    /**
     * 客户端ID
     */
    private Integer clientId;
    
    /**
     * 用户ID
     */
    private Integer userId;
    
    /**
     * 最后一次心跳时间
     */
    private long lastHeartbeatTime;

    public SessionBean(WebSocketSession webSocketSession, Integer userId) {
        this.webSocketSession = webSocketSession;
        this.userId = userId;
        this.lastHeartbeatTime = System.currentTimeMillis();
    }
}
