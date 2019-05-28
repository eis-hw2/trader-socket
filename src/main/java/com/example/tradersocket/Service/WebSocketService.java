package com.example.tradersocket.Service;

import javax.websocket.Session;
import java.io.IOException;

public interface WebSocketService {
    void onOpen(Session session, String sid, Integer bid);

    void onClose(Session session, String sid, Integer bid);

    void onMessage(String message, Session session);

    void onError(Session session, Throwable error);

    void sendMessage(String sid, String message) throws IOException;

    void broadcast(String message);

    void broadcaseById(String message, Integer borkerId);

    int getOnlineCount();
}
