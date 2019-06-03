package com.example.tradersocket.Core.BrokerSocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.tradersocket.Domain.Entity.Broker;
import com.example.tradersocket.Domain.Entity.MarketDepth;
import com.example.tradersocket.Domain.Entity.MarketQuotation;
import com.example.tradersocket.Domain.Entity.Util.SocketMessage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class BrokerSocketClient extends WebSocketClient {

    public final static short INIT = 0;
    public final static short CONNECTING = 1;
    public final static short CONNECTED = 2;
    public final static short ERROR = 3;

    private boolean closedByContainer = false;

    private Logger logger = LoggerFactory.getLogger("BrokerSocketClient");
    private int status;

    private MarketDepth marketDepth;
    private MarketQuotation marketQuotation;

    private Integer brokerId;
    private BrokerSocketContainer brokerSocketContainer;

    public BrokerSocketClient(Broker broker, BrokerSocketContainer brokerSocketContainer, String id) throws URISyntaxException{
        super(new URI(broker.getWebSocket() + "/websocket/" + id));
        logger.info("[BrokerSocketClient.Contructor] " + this.getURI());
        this.brokerSocketContainer = brokerSocketContainer;
        this.brokerId = broker.getId();
        this.status = INIT;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        logger.info("[BrokerSocket.onOpen] "+ this.uri.toString() + " Connection Success");
    }

    @Override
    public void onMessage(String msg) {
        logger.info("[BrokerSocket.onMessage] URI: " + this.uri.toString());
        logger.info("[BrokerSocket.onMessage] Message: " + msg);
        
        JSONObject body = JSON.parseObject(msg);
        String mqStr = body.getString("marketQuotation");
        String mdStr = body.getString("marketDepth");
        marketDepth = JSON.parseObject(mdStr, MarketDepth.class);
        marketQuotation = JSON.parseObject(mqStr, MarketQuotation.class);

        // quotation = body.getObject("quotation", );

        logger.info("[BrokerSocket.onMessage] MarketDepth: " + JSON.toJSONString(marketDepth));
        logger.info("[BrokerSocket.onMessage] MarketQuotation: " + JSON.toJSONString(marketQuotation));

        this.brokerSocketContainer.getWebSocketService().broadcaseById(msg, brokerId);
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        logger.info("[BrokerSocket.onClose]" + this.uri.toString() + " Connection Closed");
        brokerSocketContainer.onClose();
    }

    @Override
    public void onError(Exception e) {
        logger.info("[BrokerSocket.onError]" + this.uri.toString());
        e.printStackTrace();
    }

    public void reconnect(){
        init();
    }

    public void init(){
        this.setStatus(CONNECTING);
        logger.info("[BrokerSocket.init] " + this.uri + " Connecting");

        this.connect();
        while(!this.getReadyState().equals(READYSTATE.OPEN)){}

        this.setStatus(CONNECTED);
        logger.info("[BrokerSocket.init] " + this.uri + " Connected");

    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public MarketDepth getMarketDepth() {
        return marketDepth;
    }

    public void setMarketDepth(MarketDepth marketDepth) {
        this.marketDepth = marketDepth;
    }

    public MarketQuotation getMarketQuotation() {
        return marketQuotation;
    }

    public void setMarketQuotation(MarketQuotation marketQuotation) {
        this.marketQuotation = marketQuotation;
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
