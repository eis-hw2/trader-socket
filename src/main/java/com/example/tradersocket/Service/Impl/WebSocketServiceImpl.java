package com.example.tradersocket.Service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.tradersocket.Core.BrokerSocket.DataPair;
import com.example.tradersocket.Dao.FutureRecordDao;
import com.example.tradersocket.Domain.Entity.Broker;
import com.example.tradersocket.Domain.Entity.FutureRecord;
import com.example.tradersocket.Domain.Factory.ResponseWrapperFactory;
import com.example.tradersocket.Domain.Factory.SessionWrapperFactory;
import com.example.tradersocket.Domain.Wrapper.ResponseWrapper;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
public class WebSocketServiceImpl implements WebSocketService {

    static Logger logger = LoggerFactory.getLogger(WebSocketServiceImpl.class);

    private static CopyOnWriteArraySet<SessionWrapper> sessionWrappers = new CopyOnWriteArraySet<>();

    @Autowired
    BrokerService brokerService;
    @Autowired
    FutureRecordDao futureRecordDao;

    public static CopyOnWriteArraySet<SessionWrapper> getSessionWrappers(){
        return sessionWrappers;
    }

    @Override
    public void onOpen(Session session, @PathParam("sid") String sid) {
        /*
        String errMessage = null;
        if (sessionWrappers.stream().anyMatch(e -> e.getSid().equals(sid)))
            errMessage = "sid duplicated";

        if (errMessage!= null){
            try {
                sendMessageToSession(session, errMessage);
                session.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
            finally {
                return;
            }
        }*/

        SessionWrapper sessionWrapper = SessionWrapperFactory.create(session, sid);
        sessionWrappers.add(sessionWrapper);
        logger.info("[WebSocket.onOpen] New Connection:" + sid + ", Number of Connection:" + getOnlineCount());

        try {
            sendMessageToSession(session, ResponseWrapperFactory.createResponseString(ResponseWrapper.SUCCESS, "connect success"));
        } catch (IOException e) {
            logger.error("[WebSocket.onOpen] IO Error");
        }
    }

    @Override
    public void onClose(Session session, @PathParam("sid")String sid) {
        boolean isRemoved = sessionWrappers.removeIf(e -> e.getSid().equals(sid));
        if (isRemoved) {
            logger.info("[WebSocket.onClose] Connection Closed, Number of Connection:" + getOnlineCount());
        } else {
            logger.error("[WebSocket.onClose] Remove Error");
        }
    }

    @Override
    public void onMessage(String message, Session session, @PathParam("sid")String sid) {
        logger.info("[WebSocket.onMessage] Raw Message:"+message);
        JSONObject body = JSON.parseObject(message);

        /**
         * 用户发送 JSON config，告诉我他需要哪个broker的哪个future的信息
         */
        Integer brokerId = body.getInteger("brokerId");
        String marketDepthId = body.getString("marketDepthId");

        logger.info("[WebSocket.onMessage] BrokerId:"+brokerId);
        logger.info("[WebSocket.onMessage] MarketDepthId:"+marketDepthId);
        Broker broker = brokerService.findById(brokerId);

        for (SessionWrapper sw: sessionWrappers){
            if (sw.getSid().equals(sid)){
                sw.setBroker(broker);
                sw.setMarketDepthId(marketDepthId);
                break;
            }
        }

        Calendar curTime = Calendar.getInstance();
        curTime.set(Calendar.HOUR_OF_DAY, 0);
        curTime.set(Calendar.MINUTE, 0);
        curTime.set(Calendar.SECOND, 0);

        String startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(curTime.getTime());

        JSONObject response = new JSONObject();
        List<FutureRecord> records = futureRecordDao.findByBrokerIdAndMarketDepthIdAndDatetimeAfter(
                brokerId,
                marketDepthId,
                startTime );
        DataPair dataPair = brokerService.getDataPairByBrokerIdAndMarketDepthId(brokerId, marketDepthId);
        response.put("history", records);
        response.put("marketDepth", dataPair ==null ? null : dataPair.getMarketDepth());
        response.put("marketQuotation", dataPair ==null ? null : dataPair.getMarketQuotation());
        try{
            sendMessageToSession(session, ResponseWrapperFactory.createResponseString(ResponseWrapper.SUCCESS, response));
        }
        catch(IOException e){
            e.printStackTrace();
        }

    }

    @Override
    public void onError(Session session, Throwable error, @PathParam("sid")String sid) {
        logger.error("[WebSocket.onError] Error");
        error.printStackTrace();
    }

    @Override
    public void sendMessage(String sid, String message) throws IOException {
        logger.info("[WebSocket.sendMessage] Send Message to " + sid);
        for (SessionWrapper sessionWrapper : sessionWrappers) {
            if (sessionWrapper.getSid().equals(sid)) {
                sendMessageToSessionWrapper(sessionWrapper, message);
                return;
            }
        }
    }

    @Override
    public void broadcast(String message) {
        logger.info("[WebSocket] Send Message to All");
        logger.info(message);
        sessionWrappers.stream()
                .forEach(sessionWrapper -> {
                    try {
                        sendMessageToSessionWrapper(sessionWrapper, message);
                    } catch (IOException e) { }
                });
    }

    @Override
    public void broadcastByBrokerId(String message, Integer brokerId) {
        logger.info("[WebSocket.broadcast] BrokerId:" + brokerId);
        logger.info("[WebSocket.broadcast] Message:" + message);
        sessionWrappers.stream()
                .filter(e -> e.getBroker().getId().equals(brokerId))
                .forEach(sessionWrapper -> {
                    try {
                        sendMessageToSessionWrapper(sessionWrapper, message);
                    } catch (IOException e) { }
                });
    }

    @Override
    public void broadcastByBrokerIdAndMarketDepthId(String message, Integer brokerId, String marketDepthId) {
        logger.info("[WebSocket.broadcast] BrokerId:" + brokerId);
        logger.info("[WebSocket.broadcast] MarketDepthId:" + marketDepthId);
        logger.info("[WebSocket] Message:" + message);
        sessionWrappers.stream()
                .filter(e -> brokerId.equals(e.getBroker().getId()) &&
                                marketDepthId.equals(e.getMarketDepthId()))
                .forEach(sessionWrapper -> {
                    try {
                        sendMessageToSessionWrapper(sessionWrapper, message);
                    } catch (IOException e) { }
                });
    }


    @Override
    public synchronized int getOnlineCount() {
        return sessionWrappers.size();
    }

    private static void sendMessageToSessionWrapper(SessionWrapper sessionWrapper, String message) throws IOException {
        sendMessageToSession(sessionWrapper.getSession(), message);
    }

    private static void sendMessageToSession(Session session, String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }
}
