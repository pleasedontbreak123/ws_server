package org.example.ws_sever.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.ws_sever.entity.dto.MessageDTO;
import org.example.ws_sever.service.MessageBroadcastService;
import org.example.ws_sever.service.SocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SocketServiceImpl implements SocketService {
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    @Autowired
    private MessageBroadcastService messageBroadcastService;
    
    @Autowired
    private ObjectMapper objectMapper;

    private static final String GEO_KEY = "vehicle:locations";
    private static final double ALERT_RADIUS = 1.0; // 1千米的报警半径

    @Override
    public void handleMessage(String messageStr) {
        try {
            MessageDTO messageDTO = objectMapper.readValue(messageStr, MessageDTO.class);
            String vehicleId = messageDTO.getSenderId();
            double longitude = messageDTO.getLocation().getLongitude();
            double latitude = messageDTO.getLocation().getLatitude();

            if (messageDTO.getMessageType() == MessageDTO.MessageType.COMMON) {
                log.info("收到普通消息，更新位置信息");
                // 更新位置信息
                stringRedisTemplate.opsForGeo().add(GEO_KEY, 
                    new Point(longitude, latitude), vehicleId);
                
            } else if (messageDTO.getMessageType() == MessageDTO.MessageType.ACCIDENT) {
                log.info("收到事故消息，开始通知附近车辆");


                messageBroadcastService.broadcast(longitude, latitude, ALERT_RADIUS);
            }
        } catch (Exception e) {
            log.error("处理消息失败", e);
        }
    }

    @Override
    public void removeUserLcoation(Integer userId) {
        try {
            String vehicleId = "vehicle-" + userId;
            stringRedisTemplate.opsForGeo().remove(GEO_KEY, vehicleId);
            log.info("已移除用户 {} 的位置信息", userId);
        } catch (Exception e) {
            log.error("移除用户位置信息失败", e);
        }
    }
}
