package com.example.tradersocket.Core.BrokerSocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.tradersocket.Domain.Entity.Broker;
import com.example.tradersocket.Domain.Entity.OrderBook;
import com.example.tradersocket.Service.WebSocketService;
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

    private Logger logger = LoggerFactory.getLogger("BrokerSocketClient");
    private int status = INIT;

    private OrderBook orderBook;
    private Integer brokerId;
    private BrokerSocketContainer brokerSocketContainer;

    public BrokerSocketClient(Broker broker, BrokerSocketContainer brokerSocketContainer) throws URISyntaxException{
        super(new URI(broker.getWebSocket() + "/websocket/1"));
        this.brokerSocketContainer = brokerSocketContainer;
        this.brokerId = broker.getId();
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        logger.info("[BrokerSocket.onOpen] "+ this.uri.toString() + " Connection Success");
    }

    @Override
    public void onMessage(String msg) {
        logger.info("[BrokerSocket.onMessage] " + this.uri.toString());
        JSONObject body = JSON.parseObject(msg);

        orderBook = body.getObject("orderBook", OrderBook.class);
        // quotation = body.getObject("quotation", );

        logger.info(JSON.toJSONString(orderBook));

        this.brokerSocketContainer.getWebSocketService().broadcaseById(msg, brokerId);
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        logger.info("[BrokerSocket.onClose]" + this.uri.toString() + " Connection Closed");
        brokerSocketContainer.init();
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
        logger.info("[BrokerSocketContainer.init] " + this.uri + " Connecting");

        this.connect();
        while(!this.getReadyState().equals(READYSTATE.OPEN)){}

        this.setStatus(CONNECTED);
        logger.info("[BrokerSocketContainer.init] " + this.uri + " Connected");

    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public OrderBook getOrderBook() {
        return orderBook;
    }

    public void setOrderBook(OrderBook orderBook) {
        this.orderBook = orderBook;
    }

    public BrokerSocketContainer getBrokerSocketContainer() {
        return brokerSocketContainer;
    }

    public void setBrokerSocketContainer(BrokerSocketContainer brokerSocketContainer) {
        this.brokerSocketContainer = brokerSocketContainer;
    }
}
