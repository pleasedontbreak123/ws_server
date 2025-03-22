package org.example.ws_sever.service.impl;



import lombok.extern.slf4j.Slf4j;
import org.example.ws_sever.entity.dto.MessageDTO;
import org.example.ws_sever.service.SocketService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SocketServiceImpl implements SocketService{


    @Override
    public void handleMessage(MessageDTO messageDTO) {
        if (messageDTO.getMessageType() == MessageDTO.MyMessageType.COMMON){
            log.info("收到普通消息");

            MessageDTO.Location location = messageDTO.getLocation();
        }
        log.info("收到报警消息");
    }
}
