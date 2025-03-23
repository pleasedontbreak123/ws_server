package org.example.ws_sever;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.ws_sever.entity.dto.MessageDTO;
import org.example.ws_sever.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TestWs {
    private static final String WS_URL = "ws://localhost:8080/ws";
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void testWebSocketConnection() throws Exception {
        // 模拟生成token
        String token = generateMockToken();
        
        // 创建WebSocket客户端
        StandardWebSocketClient client = new StandardWebSocketClient();
        
        // 创建自定义的WebSocket处理器
        TextWebSocketHandler handler = new TextWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) {
                System.out.println("连接已建立");
                
                // 连接成功后开始发送消息
                new Thread(() -> {
                    try {
                        while (session.isOpen()) {
                            // 创建MessageDTO对象
                            MessageDTO messageDTO = new MessageDTO();
                            messageDTO.setSenderId("vehicle-001");
                            messageDTO.setMessageType(MessageDTO.MessageType.COMMON);
                            
                            // 创建Location对象
                            MessageDTO.Location location = MessageDTO.Location.builder()
                                .latitude(31.2304 + Math.random() * 0.01)  // 随机生成一个上海附近的位置
                                .longitude(121.4737 + Math.random() * 0.01)
                                .build();
                            messageDTO.setLocation(location);
                            
                            messageDTO.setTimestamp(LocalDateTime.now());

                            // 将对象转换为JSON字符串
                            String message = objectMapper.writeValueAsString(messageDTO);
                            session.sendMessage(new TextMessage(message));
                            System.out.println("已发送消息: " + message);

                            // 等待5秒后发送下一条消息
                            TimeUnit.SECONDS.sleep(5);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }

            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                System.out.println("收到服务器消息: " + message.getPayload());
            }
        };

        // 设置token到URL
        String wsUrlWithToken = WS_URL + "?token=" + token;
        
        // 连接到服务器
        WebSocketSession session = client.doHandshake(handler, new WebSocketHttpHeaders(), URI.create(wsUrlWithToken)).get();

        // 保持程序运行60秒
        TimeUnit.SECONDS.sleep(60);

        // 关闭连接
        session.close();
    }

    /**
     * 生成JWT token
     */
    private String generateMockToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1001);
        return JwtUtils.generateJwt(claims);
    }

    @Test
    public void testSendAccidentMessage() throws Exception {
        // 模拟生成token
        String token = generateMockToken();
        
        // 创建WebSocket客户端
        StandardWebSocketClient client = new StandardWebSocketClient();
        
        // 创建自定义的WebSocket处理器
        TextWebSocketHandler handler = new TextWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) {
                System.out.println("连接已建立");
                
                // 连接成功后发送事故消息
                try {
                    // 创建MessageDTO对象
                    MessageDTO messageDTO = new MessageDTO();
                    messageDTO.setSenderId("vehicle-002");
                    messageDTO.setMessageType(MessageDTO.MessageType.ACCIDENT);
                    
                    // 创建Location对象
                    MessageDTO.Location location = MessageDTO.Location.builder()
                        .latitude(31.2304)  // 上海某个固定位置
                        .longitude(121.4737)
                        .build();
                    messageDTO.setLocation(location);
                    
                    messageDTO.setTimestamp(LocalDateTime.now());

                    // 将对象转换为JSON字符串
                    String message = objectMapper.writeValueAsString(messageDTO);
                    session.sendMessage(new TextMessage(message));
                    System.out.println("已发送事故消息: " + message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                System.out.println("收到服务器消息: " + message.getPayload());
            }
        };

        // 设置token到URL
        String wsUrlWithToken = WS_URL + "?token=" + token;
        
        // 连接到服务器
        WebSocketSession session = client.doHandshake(handler, new WebSocketHttpHeaders(), URI.create(wsUrlWithToken)).get();

        // 等待10秒后关闭连接
        TimeUnit.SECONDS.sleep(10);

        // 关闭连接
        session.close();
    }
}
