package com.example.tradersocket.Controller;

import com.example.tradersocket.Service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;


@ServerEndpoint(value = "/websocket/{sid}/{bid}")
@Component
public class WebSocketEndpoint {

    private static WebSocketService webSocketService;

    @Autowired
    public void setWebSocketService(WebSocketService webSocketService) {
        WebSocketEndpoint.webSocketService = webSocketService;
    }


    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid, @PathParam("bid") Integer bid) {
        webSocketService.onOpen(session, sid, bid);
    }

    @OnClose
    public void onClose(Session session, @PathParam("sid") String sid, @PathParam("bid") Integer bid) {
        webSocketService.onClose(session, sid, bid);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        webSocketService.onMessage(message, session);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        webSocketService.onError(session, error);
    }
}
