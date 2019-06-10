package com.example.tradersocket.Controller;

import com.example.tradersocket.Domain.Entity.MarketQuotation;
import com.example.tradersocket.Domain.Factory.ResponseWrapperFactory;
import com.example.tradersocket.Domain.Wrapper.ResponseWrapper;
import com.example.tradersocket.Service.BrokerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/Status")
public class StatusController {
    @Autowired
    BrokerService brokerService;

    @GetMapping("/MarketQuotation")
    public ResponseWrapper getMarketQuotationByBrokerIdAndMarketDepthId(
            @RequestParam Integer brokerId, @RequestParam String marketDepthId){
        MarketQuotation mq = brokerService
                .getDataPairByBrokerIdAndMarketDepthId(brokerId, marketDepthId)
                .getMarketQuotation();
        return ResponseWrapperFactory.create(ResponseWrapper.SUCCESS, mq);
    }
}
