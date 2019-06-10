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
import com.example.tradersocket.Service.RedisService;
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

    private final static String LOGIN = "login";
    private final static String SWITCH = "switch";

    private static Logger logger = LoggerFactory.getLogger(WebSocketServiceImpl.class);

    private static CopyOnWriteArraySet<SessionWrapper> sessionWrappers = new CopyOnWriteArraySet<>();

    @Autowired
    BrokerService brokerService;
    @Autowired
    FutureRecordDao futureRecordDao;
    @Autowired
    RedisService redisService;
    @Autowired
    FutureRecordServiceImpl futureRecordService;

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
                _sendMessageToSession(session, errMessage);
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

        send(session, ResponseWrapperFactory.createResponseString(ResponseWrapper.SUCCESS, "connect success"));
    }

    @Override
    public void onClose(Session session, @PathParam("sid")String sid) {
        boolean isRemoved = sessionWrappers.removeIf(e -> e.getSid().equals(sid));
        if (isRemoved) {
            logger.info("[WebSocket.onClose] Connection Closed, Number of Connection:" + getOnlineCount());
        } else {
            logger.error("[WebSocket.onClose] Remove Error, Number of Connection:" + getOnlineCount());
        }
    }

    @Override
    public void onMessage(String message, Session session, @PathParam("sid")String sid) {
        logger.info("[WebSocket.onMessage] Raw Message:"+message);
        JSONObject msg = JSON.parseObject(message);

        String commandType = msg.getString("type");
        switch (commandType){
            case LOGIN:
            {
                SessionWrapper cur = getSessionWrapperBySid(sid);
                if (cur == null) {
                    send(session, ResponseWrapperFactory.createResponseString(
                            ResponseWrapper.ERROR, "SessionWrapper not found"));
                    return;
                }
                JSONObject body = msg.getJSONObject("body");
                boolean success = loginProcess(cur, body);
                if (!success){
                    send(session, ResponseWrapperFactory.createResponseString(
                            ResponseWrapper.ERROR, "Login Failure"));
                    return;
                }
                switchProcess(cur, body);
                break;
            }
            case SWITCH:
            {
                /**
                 * 用户发送 brokerId & marketDepthId
                 * 告诉我他需要哪个broker的哪个future的信息
                 */
                SessionWrapper cur = getSessionWrapperBySid(sid);
                if (cur == null) {
                    send(session, ResponseWrapperFactory.createResponseString(
                            ResponseWrapper.ERROR, "SessionWrapper not found"));
                    break;
                } else if (!cur.isLogin()) {
                    send(session, ResponseWrapperFactory.createResponseString(
                            ResponseWrapper.ERROR, "Please Login"));
                    break;
                }

                JSONObject body = msg.getJSONObject("body");

                switchProcess(cur, body);

                break;
            }
            default:
                send(session, ResponseWrapperFactory.createResponseString(
                        ResponseWrapper.ERROR, "Unknown type:" + commandType));
        }
    }

    private SessionWrapper getSessionWrapperBySid(String sid){
        for (SessionWrapper sw : sessionWrappers) {
            if (sw.getSid().equals(sid)) {
                return sw;
            }
        }
        return null;
    }

    private boolean loginProcess(SessionWrapper cur, JSONObject body){
        String username = body.getString("username");
        String token = body.getString("token");
        String tokenInRedis = (String)redisService.get(username);
        if (tokenInRedis.equals(token)) {
            cur.setLogin(true);
            send(cur, ResponseWrapperFactory.createResponseString(
                    ResponseWrapper.SUCCESS, "Login Success"));
            return true;
        }
        else {
            send(cur, ResponseWrapperFactory.createResponseString(
                    ResponseWrapper.ERROR, "Login Failure"));
            return false;
        }
    }

    private void switchProcess(SessionWrapper cur, JSONObject body){
        int brokerId = body.getIntValue("brokerId");
        String marketDepthId = body.getString("marketDepthId");

        logger.info("[WebSocket.onMessage] BrokerId:" + brokerId);
        logger.info("[WebSocket.onMessage] MarketDepthId:" + marketDepthId);
        Broker broker = brokerService.findById(brokerId);

        cur.setBroker(broker);
        cur.setMarketDepthId(marketDepthId);
        cur.setIntervalSecond(body.getIntValue("intervalSecond"));

        Calendar curTime = Calendar.getInstance();
        curTime.set(Calendar.HOUR_OF_DAY, 0);
        curTime.set(Calendar.MINUTE, 0);
        curTime.set(Calendar.SECOND, 0);

        String startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(curTime.getTime());

        JSONObject response = new JSONObject();
        int intervalSecond = cur.getIntervalSecond();
        List<FutureRecord> records;
        if (intervalSecond <= 0)
            records = futureRecordDao.findByBrokerIdAndMarketDepthIdAndDatetimeAfter(
                brokerId,
                marketDepthId,
                startTime);
        else
            records = futureRecordService.findByBrokerIdAndMarketDepthIdAndDatetimeAfter(
                    brokerId,
                    marketDepthId,
                    startTime,
                    intervalSecond
            );
        DataPair dataPair = brokerService.getDataPairByBrokerIdAndMarketDepthId(brokerId, marketDepthId);
        response.put("history", records);
        if (dataPair == null){
            response.put("marketDepth", null);
            response.put("marketQuotation", null);
            response.put("curVolume", null);
        }
        else{
            response.put("marketDepth", dataPair.getMarketDepth());
            response.put("marketQuotation", dataPair.getMarketQuotation());
            response.put("curVolume", dataPair.getCurVolume());
        }

        send(cur, ResponseWrapperFactory.createResponseString(
                ResponseWrapper.SUCCESS, response));
    }

    @Override
    public void onError(Session session, Throwable error, @PathParam("sid")String sid) {
        logger.error("[WebSocket.onError] Error");
        error.printStackTrace();
    }

    @Override
    public void sendMessage(String sid, String message) {
        logger.info("[WebSocket.sendMessage] Send Message to " + sid);
        for (SessionWrapper sessionWrapper : sessionWrappers) {
            if (sessionWrapper.getSid().equals(sid)) {
                send(sessionWrapper, message);
                return;
            }
        }
    }

    @Override
    public void broadcast(String message) {
        logger.info("[WebSocket] Send Message to All");
        logger.info(message);
        sessionWrappers.stream()
                .filter(e -> e.isLogin())
                .forEach(sessionWrapper -> {
                    send(sessionWrapper, message);
                });
    }

    @Override
    public void broadcastByBrokerId(String message, Integer brokerId) {
        logger.info("[WebSocket.broadcast] BrokerId:" + brokerId);
        logger.info("[WebSocket.broadcast] Message:" + message);
        sessionWrappers.stream()
                .filter(e -> e.isLogin()
                        && e.getBroker() != null
                        && e.getBroker().getId().equals(brokerId))
                .forEach(sessionWrapper -> {
                    send(sessionWrapper, message);
                });
    }

    @Override
    public void broadcastByBrokerIdAndMarketDepthId(String message, Integer brokerId, String marketDepthId) {
        logger.info("[WebSocket.broadcast] BrokerId:" + brokerId);
        logger.info("[WebSocket.broadcast] MarketDepthId:" + marketDepthId);
        logger.info("[WebSocket] Message:" + message);
        sessionWrappers.stream()
                .filter(e -> e.isLogin()
                        && e.getBroker() != null
                        && brokerId.equals(e.getBroker().getId())
                        && marketDepthId.equals(e.getMarketDepthId()))
                .forEach(sessionWrapper -> {
                    send(sessionWrapper, message);
                });
    }


    @Override
    public synchronized int getOnlineCount() {
        return sessionWrappers.size();
    }

    private static void _sendMessageToSessionWrapper(SessionWrapper sessionWrapper, String message) throws IOException {
        _sendMessageToSession(sessionWrapper.getSession(), message);
    }

    private static void _sendMessageToSession(Session session, String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    private static void send(Session session, String message){
        try {
            _sendMessageToSession(session, message);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private static void send(SessionWrapper sw, String message){
        try {
            _sendMessageToSessionWrapper(sw, message);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
