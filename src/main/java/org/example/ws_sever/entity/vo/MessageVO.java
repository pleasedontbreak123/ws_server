package org.example.ws_sever.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.ws_sever.entity.dto.MessageDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageVO {

    private MessageDTO.Location location;

    private double distance;

    private String aiRecommend;
}
