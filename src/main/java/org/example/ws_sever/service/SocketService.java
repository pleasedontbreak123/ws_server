package org.example.ws_sever.service;

import org.example.ws_sever.entity.dto.MessageDTO;

public interface SocketService {

    void handleMessage(MessageDTO messageDTO);
}
