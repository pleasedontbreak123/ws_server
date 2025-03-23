package org.example.ws_sever.service;

public interface SocketService {

    void handleMessage(String messageStr);

    void removeUserLcoation(Integer userId);
}
