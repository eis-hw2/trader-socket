package com.example.tradersocket.Service;

import javax.websocket.Session;
import java.io.IOException;

public interface WebSocketService {
    void onOpen(Session session, String sid);

    void onClose(Session session, String sid);

    void onMessage(String message, Session session, String sid);

    void onError(Session session, Throwable error, String sid);

    void sendMessage(String sid, String message) throws IOException;

    void broadcast(String message);

    void broadcastByBrokerId(String message, Integer borkerId);

    void broadcastByBrokerIdAndMarketDepthId(String message, Integer borkerId, String marketDepthId);

    int getOnlineCount();
}
