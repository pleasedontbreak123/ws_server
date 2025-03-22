package org.example.ws_sever.entity.dto;

import lombok.Data;
import org.example.ws_sever.entity.po.Message;

import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private String senderId;

    private MyMessageType messageType;
    public enum MyMessageType
    {
        COMMON,
        ACCIDENT
    }

    private Location location;

    private LocalDateTime timestamp;

    @Data
    public class Location{
        private double latitude;  // 纬度
        private double longitude; // 经度
    }
}
