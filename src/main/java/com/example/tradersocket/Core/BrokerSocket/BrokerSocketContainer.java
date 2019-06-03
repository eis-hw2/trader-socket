package com.example.tradersocket.Core.BrokerSocket;

import com.example.tradersocket.Domain.Entity.Broker;
import com.example.tradersocket.Domain.Entity.MarketDepth;
import com.example.tradersocket.Service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.UUID;

public class BrokerSocketContainer {
    private Logger logger = LoggerFactory.getLogger("BrokerSocketContainer");

    private BrokerSocketClient client;
    private Broker broker;
    private WebSocketService webSocketService;
    private UUID id;

    public BrokerSocketContainer(Broker broker, WebSocketService webSocketService){
        this.id = UUID.randomUUID();
        this.broker = broker;
        this.webSocketService = webSocketService;
        logger.info("[BrokerSocketContainer.Constructor] " + broker.getWebSocket() + " " + this.id);
    }

    public void init(){
        try {
            if (client != null && client.isOpen()) {
                client.close();
                client.setClosedByContainer(true);
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
    }

    public Broker getBroker() {
        return broker;
    }

    public MarketDepth getMarketDepth(){
        return client.getMarketDepth();
    }

    @Override
    protected void finalize() throws Throwable {
        if (client != null)
            client.close();
    }

    public WebSocketService getWebSocketService() {
        return webSocketService;
    }

    public void setWebSocketService(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }
}
