package com.example.tradersocket.Service.Impl;


import com.alibaba.fastjson.JSON;
import com.example.tradersocket.Core.BrokerSocket.BrokerSocketContainer;
import com.example.tradersocket.Dao.BrokerDao;
import com.example.tradersocket.Domain.Entity.Broker;
import com.example.tradersocket.Domain.Entity.MarketDepth;
import com.example.tradersocket.Service.BrokerService;
import com.example.tradersocket.Service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BrokerServiceImpl implements BrokerService {
    private static ConcurrentHashMap<Integer, BrokerSocketContainer> brokerSocketContainers = new ConcurrentHashMap<>();

    private static Logger logger = LoggerFactory.getLogger("BrokerService");

    @Autowired
    private BrokerDao brokerDao;
    @Autowired
    private WebSocketService webSocketService;

    @PostConstruct
    public void init(){
        System.out.println("[BrokerService.init] init");
        List<Broker> brokers = brokerDao.findAll();
        brokers.stream().forEach( e -> {
            logger.info("[BrokerService.init] broker: "+JSON.toJSONString(e));
            BrokerSocketContainer brokerSocket = new BrokerSocketContainer(e, webSocketService);
            brokerSocket.init();
            brokerSocketContainers.put(e.getId(), brokerSocket);
        });
    }

    @Override
    public Broker create(Broker broker){
        Broker b = brokerDao.save(broker);
        BrokerSocketContainer brokerSocket = new BrokerSocketContainer(b, webSocketService);
        //brokerSocket.init();
        brokerSocketContainers.put(b.getId(), brokerSocket);
        return b;
    }

    @Override
    public boolean deleteById(Integer id){
        brokerSocketContainers.get(id).close();
        brokerSocketContainers.remove(id);
        brokerDao.deleteById(id);
        return true;
    }

    @Override
    public List<Broker> findAll(){
        return brokerDao.findAll();
    }

    @Override
    public Broker findById(Integer id) {
        BrokerSocketContainer bsc = brokerSocketContainers.get(id);
        if (bsc == null) {
            return brokerDao.findById(id).get();
        }
        else
            return bsc.getBroker();
    }
}
