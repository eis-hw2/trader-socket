package com.example.tradersocket.Dao;

import com.example.tradersocket.Domain.Entity.FutureRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FutureRecordDao extends JpaRepository<FutureRecord, Integer>{

    @Override
    List<FutureRecord> findAll();

    List<FutureRecord> findByBrokerIdAndMarketDepthIdAndDatetimeBetween(
            Integer brokerId,
            String marketDepthId,
            String startTime,
            String endTime
    );

    List<FutureRecord> findByBrokerIdAndMarketDepthIdAndDatetimeAfter(
            Integer brokerId,
            String marketDepthId,
            String startTime
    );
}
