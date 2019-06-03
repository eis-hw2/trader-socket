package com.example.tradersocket.Service.Impl;

import com.alibaba.fastjson.JSON;
import com.example.tradersocket.Domain.Entity.Broker;
import com.example.tradersocket.Domain.Factory.SessionWrapperFactory;
import com.example.tradersocket.Domain.Wrapper.SessionWrapper;
import com.example.tradersocket.Service.BrokerService;
import com.example.tradersocket.Service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.websocket.Session;
import javax.websocket.server.PathParam;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
public class WebSocketServiceImpl implements WebSocketService {

    static Logger log = LoggerFactory.getLogger(WebSocketServiceImpl.class);

    private static CopyOnWriteArraySet<SessionWrapper> sessionWrappers = new CopyOnWriteArraySet<>();
    private static Integer onlineCount = 0;

    @Autowired
    BrokerService brokerService;

    public static CopyOnWriteArraySet<SessionWrapper> getSessionWrappers(){
        return sessionWrappers;
    }

    @Override
    public void onOpen(Session session, @PathParam("sid") String sid, @PathParam("bid") Integer bid) {
        Broker broker = brokerService.findById(bid);
        String errMessage = null;
        if (sessionWrappers.stream().anyMatch(e -> e.getSid().equals(sid)))
            errMessage = "sid duplicated";
        else if (broker == null)
            errMessage = "broker not found";

        if (errMessage!= null){
            try {
                sendMessageToSession(session, errMessage);
                session.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
            return;
        }
        SessionWrapper sessionWrapper = SessionWrapperFactory.create(session, sid, broker);
        sessionWrappers.add(sessionWrapper);
        addOnlineCount();
        log.info("[WebSocket.onOpen] New Connection:" + sid + ", Number of Connection:" + getOnlineCount());

        try {
            sendMessageToSession(session, JSON.toJSONString(brokerService.findOrderBookByBrokerId(bid)));
        } catch (IOException e) {
            log.error("[WebSocket.onOpen] IO Error");
        }
    }

    @Override
    public void onClose(Session session, String sid, @PathParam("bid") Integer bid) {
        boolean isRemoved = sessionWrappers.removeIf(e -> e.getSid().equals(sid));
        if (isRemoved) {
            subOnlineCount();
            log.info("[WebSocket.onClose] Connection Closed, Number of Connection:" + getOnlineCount());
        } else {
            log.error("[WebSocket.onClose] Remove Error");
        }
    }

    @Override
    public void onMessage(String message, Session session) {
        try{
            sendMessageToSession(session, message);
        }
        catch(IOException e){
            e.printStackTrace();
        }

    }

    @Override
    public void onError(Session session, Throwable error) {
        log.error("[WebSocket.onError] Error");
        error.printStackTrace();
    }

    @Override
    public void sendMessage(String sid, String message) throws IOException {
        log.info("[WebSocket.sendMessage] Send Message to " + sid);
        for (SessionWrapper sessionWrapper : sessionWrappers) {
            if (sessionWrapper.getSid().equals(sid)) {
                sendMessageToSessionWrapper(sessionWrapper, message);
                return;
            }
        }
    }

    @Override
    public void broadcast(String message) {
        log.info("[WebSocket] Send Message to All");
        log.info(message);
        sessionWrappers.stream()
                .forEach(sessionWrapper -> {
                    try {
                        sendMessageToSessionWrapper(sessionWrapper, message);
                    } catch (IOException e) { }
                });
    }

    @Override
    public void broadcaseById(String message, Integer brokerId) {
        log.info("[WebSocket.broadcast] Send Message By BrokerId:" + brokerId);
        log.info("[WebSocket] Message: " + message);
        sessionWrappers.stream()
                .filter(e -> e.getBroker().getId().equals(brokerId))
                .forEach(sessionWrapper -> {
                    try {
                        sendMessageToSessionWrapper(sessionWrapper, message);
                    } catch (IOException e) { }
                });
    }


    @Override
    public synchronized int getOnlineCount() {
        return onlineCount;
    }

    private static void sendMessageToSessionWrapper(SessionWrapper sessionWrapper, String message) throws IOException {
        sendMessageToSession(sessionWrapper.getSession(), message);
    }

    private static void sendMessageToSession(Session session, String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    private void addOnlineCount() {
        synchronized (onlineCount) {
            WebSocketServiceImpl.onlineCount++;
        }
    }

    private void subOnlineCount() {
        synchronized (onlineCount) {
            WebSocketServiceImpl.onlineCount--;
        }
    }
}
