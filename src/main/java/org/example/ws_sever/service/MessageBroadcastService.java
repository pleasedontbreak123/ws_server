package org.example.ws_sever.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public interface MessageBroadcastService {
    void broadcast(double longitude, double latitude, double radius) throws IOException;
    void sendToUser(Integer userId, String message);
    void registerSession(Integer userId, WebSocketSession session);
    void removeSession(Integer userId);
} 