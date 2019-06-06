package com.example.tradersocket.Service.Impl;


import com.alibaba.fastjson.JSON;
import com.example.tradersocket.Core.BrokerSocket.BrokerSocketContainer;
import com.example.tradersocket.Core.BrokerSocket.DataPair;
import com.example.tradersocket.Dao.BrokerDao;
import com.example.tradersocket.Dao.FutureRecordDao;
import com.example.tradersocket.Domain.Entity.Broker;
import com.example.tradersocket.Domain.Entity.MarketDepth;
import com.example.tradersocket.Exception.BrokerSocketNotExistException;
import com.example.tradersocket.Service.BrokerService;
import com.example.tradersocket.Service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class BrokerServiceImpl implements BrokerService {
    /**
     * Key: BrokerId
     * Value: BrokerSocketContainer
     */
    private static ConcurrentHashMap<Integer, BrokerSocketContainer> brokerSocketContainers = new ConcurrentHashMap<>();

    private static Logger logger = LoggerFactory.getLogger("BrokerService");

    @Autowired
    private BrokerDao brokerDao;
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private ExecutorService pool;
    @Autowired
    private FutureRecordDao futureRecordDao;

    @Bean
    public ExecutorService pool(){
        return Executors.newCachedThreadPool();
    }

    @Override
    public DataPair getDataPairByBrokerIdAndMarketDepthId(Integer brokerId, String marketDepthId){
        BrokerSocketContainer bsc = brokerSocketContainers.get(brokerId);
        if (bsc == null)
            throw new BrokerSocketNotExistException("BrokerSocket "+brokerId+" not exist");
        return bsc.getDataPairByMarketDepthId(marketDepthId);
    }

    private void socketInit(Broker broker){
        logger.info("[BrokerService.init] start BrokerId: " + broker.getId());
        BrokerSocketContainer brokerSocket = new BrokerSocketContainer(broker, webSocketService, futureRecordDao);
        brokerSocketContainers.put(broker.getId(), brokerSocket);
        brokerSocket.init();
        logger.info("[BrokerService.init] end BrokerId: " + broker.getId());
    }

    @PostConstruct
    public void init(){
        List<Broker> brokers = brokerDao.findAll();

        brokers.stream().forEach(e -> {
            pool.execute(() -> {
                socketInit(e);
            });
        });

    }

    @Override
    public Broker create(Broker broker){
        Broker savedBroker = brokerDao.save(broker);

        pool.execute(() -> {
            socketInit(savedBroker);
        });

        return savedBroker;
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
