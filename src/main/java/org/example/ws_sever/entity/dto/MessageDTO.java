package org.example.ws_sever.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageDTO {
    
    @NotBlank(message = "发送者ID不能为空")
    private String senderId;

    @NotNull("消息类型不能为空")
    private MessageType messageType;

    @NotNull("位置信息不能为空")
    private Location location;

    @NotNull("时间戳不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public enum MessageType {
        COMMON("普通消息"),
        ACCIDENT("事故消息");

        private final String description;

        MessageType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        @NotNull("纬度不能为空")
        private Double latitude;  // 纬度

        @NotNull("经度不能为空")
        private Double longitude; // 经度

        /**
         * 验证坐标是否有效
         * @return 坐标是否在有效范围内
         */
        public boolean isValid() {
            return latitude != null && longitude != null 
                && latitude >= -90 && latitude <= 90 
                && longitude >= -180 && longitude <= 180;
        }
    }
}
