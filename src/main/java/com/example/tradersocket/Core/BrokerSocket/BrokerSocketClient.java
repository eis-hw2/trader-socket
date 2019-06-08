package com.example.tradersocket.Core.BrokerSocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.tradersocket.Domain.Entity.Broker;
import com.example.tradersocket.Domain.Entity.FutureRecord;
import com.example.tradersocket.Domain.Entity.MarketDepth;
import com.example.tradersocket.Domain.Entity.MarketQuotation;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class BrokerSocketClient extends WebSocketClient {

    public final static short INIT = 0;
    public final static short CONNECTING = 1;
    public final static short CONNECTED = 2;
    public final static short ERROR = 3;

    private final static String MarketQuotation = "marketQuotation";
    private final static String MarketDepth = "marketDepth";
    private final static String Timestamp = "timestamp";
    private final static String CurPrice = "curPrice";
    private final static String CurVolume = "curVolume";
    private final static String CurTime = "curTime";

    private boolean closedByContainer = false;

    private Logger logger = LoggerFactory.getLogger("BrokerSocketClient");
    private int status;

    /**
     * Key: MarketDepthId
     * Value: MarketDepth & MarketQuotation
     */
    private Map<String, DataPair> lastDataPair = new HashMap<>();
    private Integer lastTotalVolume = 0;
    private Calendar lastTime = Calendar.getInstance();

    private Integer brokerId;
    private BrokerSocketContainer brokerSocketContainer;

    public DataPair getDataPairByMarketDepthId(String marketDepthId){
        return lastDataPair.get(marketDepthId);
    }

    public BrokerSocketClient(Broker broker, BrokerSocketContainer brokerSocketContainer, String id) throws URISyntaxException{
        super(new URI(broker.getWebSocket() + "/websocket/" + id));
        logger.info("[BrokerSocketClient.Contructor] " + this.getURI());
        this.brokerSocketContainer = brokerSocketContainer;
        this.brokerId = broker.getId();
        this.status = INIT;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        closedByContainer = false;
        logger.info("[BrokerSocketClient.onOpen] "+ this.uri.toString() + " Connection Success");
    }

    @Override
    public void onMessage(String msg) {
        logger.info("[BrokerSocketClient.onMessage] URI: " + this.uri.toString());
        logger.info("[BrokerSocketClient.onMessage] Message: " + msg);
        
        JSONObject body = JSON.parseObject(msg);
        String mqStr = body.getString(MarketQuotation);
        String mdStr = body.getString(MarketDepth);
        long timestamp = body.getLongValue(Timestamp);

        MarketDepth marketDepth = JSON.parseObject(mdStr, MarketDepth.class);
        MarketQuotation marketQuotation = JSON.parseObject(mqStr, MarketQuotation.class);

        logger.info("[BrokerSocketClient.onMessage] MarketDepth: " + JSON.toJSONString(marketDepth));
        logger.info("[BrokerSocketClient.onMessage] MarketQuotation: " + JSON.toJSONString(marketQuotation));

        String marketDepthId = marketDepth.getId();
        /**
         * 状态信息
         */
        DataPair curData = new DataPair();
        curData.setMarketDepth(marketDepth);
        curData.setMarketQuotation(marketQuotation);
        curData.setTimestamp(timestamp);

        /**
         * 持久化信息
         */
        float curPrice = marketQuotation.getChangePrice();
        Calendar curTime = Calendar.getInstance();
        int curVolume;
        int curTotalVolume = marketQuotation.getTotalVolume();
        // 判断是否是新的一天
        if (curTime.get(Calendar.DAY_OF_WEEK) != lastTime.get(Calendar.DAY_OF_WEEK))
            curVolume = curTotalVolume;
        else
            curVolume = curTotalVolume - lastTotalVolume;

        lastTotalVolume = curTotalVolume;
        lastTime = curTime;
        lastDataPair.put(marketDepthId, curData);
        logger.info("[BrokerSocketClient.onMessage] lastTotalVolume update: " + lastTotalVolume);
        logger.info("[BrokerSocketClient.onMessage] lastTime update: " + lastTime);
        logger.info("[BrokerSocketClient.onMessage] lastDataPair update: ("+marketDepthId+", "+JSON.toJSONString(curData)+")");


        String datetime =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(curTime.getTime());

        /**
         * 持久化信息装载
         */
        FutureRecord futureRecord = new FutureRecord();
        futureRecord.setBrokerId(brokerId);
        futureRecord.setDatetime(datetime);
        futureRecord.setMarketDepthId(marketDepthId);
        futureRecord.setPrice(curPrice);
        futureRecord.setVolume(curVolume);
        futureRecord.setTimestamp(timestamp);
        this.getBrokerSocketContainer().getFutureRecordDao().save(futureRecord);

        /**
         * 转发信息
         */
        JSONObject retweet = new JSONObject();
        retweet.put(CurPrice, curPrice);
        retweet.put(CurVolume, curVolume);
        retweet.put(CurTime, datetime);
        retweet.put(MarketQuotation, marketQuotation);
        retweet.put(MarketDepth, marketDepth);
        retweet.put(Timestamp, timestamp);

        this.getBrokerSocketContainer().getWebSocketService().broadcastByBrokerIdAndMarketDepthId(retweet.toJSONString(), brokerId, marketDepthId);
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        logger.info("[BrokerSocketClient.onClose]" + this.uri.toString() + " Connection Closed");
        brokerSocketContainer.onClose();
    }

    @Override
    public void onError(Exception e) {
        logger.info("[BrokerSocketClient.onError]" + this.uri.toString());
        e.printStackTrace();
    }

    public void reconnect(){
        init();
    }

    public void init(){
        this.setStatus(CONNECTING);
        logger.info("[BrokerSocketClient.init] " + this.uri + " Connecting");

        this.connect();
        while(!this.getReadyState().equals(READYSTATE.OPEN)){}

        this.setStatus(CONNECTED);
        logger.info("[BrokerSocketClient.init] " + this.uri + " Connected");

    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public BrokerSocketContainer getBrokerSocketContainer() {
        return brokerSocketContainer;
    }

    public void setBrokerSocketContainer(BrokerSocketContainer brokerSocketContainer) {
        this.brokerSocketContainer = brokerSocketContainer;
    }

    public boolean isClosedByContainer() {
        return closedByContainer;
    }

    public void setClosedByContainer(boolean closedByContainer) {
        this.closedByContainer = closedByContainer;
    }
}
