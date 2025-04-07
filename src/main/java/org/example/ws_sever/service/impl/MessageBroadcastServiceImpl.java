package org.example.ws_sever.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.ws_sever.entity.dto.MessageDTO;
import org.example.ws_sever.entity.vo.MessageVO;
import org.example.ws_sever.service.MessageBroadcastService;
import org.example.ws_sever.utils.AiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class MessageBroadcastServiceImpl implements MessageBroadcastService {
    
    private final Map<Integer, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private AiClient aiClient;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final String GEO_KEY = "vehicle:locations";

    /**
     * 广播消息
     * 向方圆内的在线用户发送报警信息
     *
     * @param longitude
     * @param latitude
     * @param radius  半径
     */
    @Override
    public void broadcast(double longitude, double latitude, double radius) throws IOException {
        Circle circle = new Circle(new Point(longitude, latitude), 
                                 new Distance(radius, Metrics.KILOMETERS));
        
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = 
            stringRedisTemplate.opsForGeo().radius(GEO_KEY, circle);

        log.info(results.toString());

        if (results != null) {
            for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results) {
                String vehicleId = result.getContent().getName();
                log.info("用户是"+vehicleId);
                double distance = result.getDistance().getValue();

                String aiRecommendation = "AI"+aiClient.getChatGPTResponse(results,result) ;

                MessageDTO.Location location = new MessageDTO.Location(longitude, latitude);
                MessageVO message = new MessageVO(location, distance,aiRecommendation);
                String messageJosn = objectMapper.writeValueAsString(message);


                try {
                    String userId = vehicleId.split("-")[1];
                    sendToUser(Integer.parseInt(userId), messageJosn);
                    log.info("已向车辆 {} 发送消息，距离 {} 公里", vehicleId, String.format("%.2f", distance));
                } catch (Exception e) {
                    log.error("发送消息失败: {}", e.getMessage());
                }
            }
        }
    }

    @Override
    public void sendToUser(Integer userId, String message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                log.error("发送消息给用户 {} 失败", userId, e);
                removeSession(userId);
            }
        }
    }

    @Override
    public void registerSession(Integer userId, WebSocketSession session) {
        sessions.put(userId, session);
        log.info("注册会话：用户 {}，当前在线数量：{}", userId, sessions.size());
    }

    @Override
    public void removeSession(Integer userId) {
        sessions.remove(userId);
        log.info("移除会话：用户 {}，当前在线数量：{}", userId, sessions.size());
    }
} 