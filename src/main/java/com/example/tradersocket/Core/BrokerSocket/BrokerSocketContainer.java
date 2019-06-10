package com.example.tradersocket.Core.BrokerSocket;

import com.alibaba.fastjson.JSONObject;
import com.example.tradersocket.Dao.FutureRecordDao;
import com.example.tradersocket.Domain.Entity.Broker;
import com.example.tradersocket.Domain.Entity.FutureRecord;
import com.example.tradersocket.Domain.Entity.MarketDepth;
import com.example.tradersocket.Service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

public class BrokerSocketContainer {
    public final static short INIT = 0;
    public final static short CONNECTING = 1;
    public final static short CONNECTED = 2;
    public final static short ERROR = 3;

    private Logger logger = LoggerFactory.getLogger("BrokerSocketContainer");

    private BrokerSocketClient client;
    private Broker broker;
    private WebSocketService webSocketService;
    private FutureRecordDao futureRecordDao;
    private UUID id;

    // 存储的状态信息
    private Map<String, DataPair> lastDataPair = new HashMap<>();
    private int status;
    private ExecutorService pool;

    // 实际发送给用户的封装过的信息
    private Map<String, JSONObject> toRetweet = new HashMap<>();

    public BrokerSocketContainer(Broker broker,
                                 WebSocketService webSocketService,
                                 FutureRecordDao futureRecordDao,
                                 ExecutorService pool){
        this.pool = pool;
        this.id = UUID.randomUUID();
        this.broker = broker;
        this.futureRecordDao = futureRecordDao;
        this.webSocketService = webSocketService;
        logger.info("[BrokerSocketContainer.Constructor] " + broker.getWebSocket() + " " + this.id);
    }

    public void init(){
        try {
            if (client != null && client.isOpen()) {
                this.close();
            }
            client = new BrokerSocketClient(broker, this, id.toString());
            client.init();
        }
        catch(URISyntaxException e){
            e.printStackTrace();
            logger.info("[BrokerSocketContainer] Error");
            logger.info("[BrokerSocketContainer] Reconnecting... ");
            init();
        }
    }

    public void onClose(){
        if (!client.isClosedByContainer()){
            this.init();
        }
    }

    public void send(byte[] bytes){
        client.send(bytes);
    }

    public void close(){
        client.close();
        client.setClosedByContainer(true);
    }

    public Broker getBroker() {
        return broker;
    }

    @Override
    protected void finalize() throws Throwable {
        if (client != null)
            this.close();
    }

    public WebSocketService getWebSocketService() {
        return webSocketService;
    }

    public void setWebSocketService(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    public FutureRecordDao getFutureRecordDao() {
        return futureRecordDao;
    }

    public void setFutureRecordDao(FutureRecordDao futureRecordDao) {
        this.futureRecordDao = futureRecordDao;
    }

    public DataPair getDataPairByMarketDepthId(String marketDepthId){
        return lastDataPair.get(marketDepthId);
    }

    private void broadcast(String msg, String marketDepthId){
        webSocketService.broadcastByBrokerIdAndMarketDepthId(
                msg,
                this.broker.getId(),
                marketDepthId);
    }

    public void broadcastByMarketDepthId(String marketDepthId){
        String msg = toRetweet.get(marketDepthId).toJSONString();
        broadcast(msg, marketDepthId);
    }

    public void broadcastAll(){
        toRetweet.entrySet().stream().forEach(e -> {
            String msg = e.getValue().toJSONString();
            String marketDepthId = e.getKey();

            //logger.info("[BrokerSocketContainer.broadcastAll] "+broker.getId()+"."+marketDepthId+":"+msg);
            broadcast(msg, marketDepthId);
        });
    }

    public void saveFutureRecord(FutureRecord fr){
        pool.execute(() -> {
            futureRecordDao.save(fr);
        });
    }

    public void resetStatus(){
        lastDataPair = new HashMap<>();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Map<String, DataPair> getLastDataPair() {
        return lastDataPair;
    }

    public void setLastDataPair(Map<String, DataPair> lastDataPair) {
        this.lastDataPair = lastDataPair;
    }

    public Map<String, JSONObject> getToRetweet() {
        return toRetweet;
    }

    public void setToRetweet(Map<String, JSONObject> toRetweet) {
        this.toRetweet = toRetweet;
    }
}
