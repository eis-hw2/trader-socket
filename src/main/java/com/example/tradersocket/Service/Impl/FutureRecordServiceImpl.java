package com.example.tradersocket.Service.Impl;

import com.example.tradersocket.Dao.FutureRecordDao;
import com.example.tradersocket.Domain.Entity.FutureRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
public class FutureRecordServiceImpl {
    @Autowired
    private FutureRecordDao futureRecordDao;
    public List<FutureRecord> findByBrokerIdAndMarketDepthIdAndDatetimeAfter(
            Integer brokerId,
            String marketDepthId,
            String startTime,
            Integer intervalSecond){
        List<FutureRecord> raw = futureRecordDao.findByBrokerIdAndMarketDepthIdAndDatetimeAfter(
                brokerId, marketDepthId, startTime);

        if (intervalSecond == null)
            return raw;
        List<FutureRecord> processed = new ArrayList<>();

        long lastTime = 0;
        for (FutureRecord fr: raw){
            if (fr.getTimestamp() - lastTime > intervalSecond * 1000){
                processed.add(fr);
                lastTime = fr.getTimestamp();
            }
        }
        return processed;
    }
}
