package com.example.tradersocket.Service;

import com.example.tradersocket.Domain.Entity.Broker;
import com.example.tradersocket.Domain.Entity.MarketDepth;

import java.util.List;

public interface BrokerService {
    Broker create(Broker broker);

    boolean deleteById(Integer id);

    List<Broker> findAll();

    Broker findById(Integer id);
}
