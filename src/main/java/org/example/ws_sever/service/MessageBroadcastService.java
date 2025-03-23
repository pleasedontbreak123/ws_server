package org.example.ws_sever.service;

import org.springframework.web.socket.WebSocketSession;

public interface MessageBroadcastService {
    void broadcast(double longitude, double latitude, double radius);
    void sendToUser(Integer userId, String message);
    void registerSession(Integer userId, WebSocketSession session);
    void removeSession(Integer userId);
} 