package com.example.tradersocket.Controller;

import com.example.tradersocket.Service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;


@ServerEndpoint(value = "/websocket/{sid}")
@Component
public class WebSocketEndpoint {

    private static WebSocketService webSocketService;

    @Autowired
    public void setWebSocketService(WebSocketService webSocketService) {
        WebSocketEndpoint.webSocketService = webSocketService;
    }


    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        webSocketService.onOpen(session, sid);
    }

    @OnClose
    public void onClose(Session session, @PathParam("sid") String sid) {
        webSocketService.onClose(session, sid);
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("sid") String sid) {
        webSocketService.onMessage(message, session, sid);
    }

    @OnError
    public void onError(Session session, Throwable error, @PathParam("sid") String sid) {
        webSocketService.onError(session, error, sid);
    }
}
