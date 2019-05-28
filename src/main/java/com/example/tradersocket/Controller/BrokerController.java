package com.example.tradersocket.Controller;

import com.example.tradersocket.Domain.Entity.Broker;
import com.example.tradersocket.Domain.Factory.ResponseWrapperFactory;
import com.example.tradersocket.Domain.Wrapper.ResponseWrapper;
import com.example.tradersocket.Service.BrokerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/Broker")
public class BrokerController {
    @Autowired
    BrokerService brokerService;

    @PostMapping("")
    public ResponseWrapper create(@RequestBody Broker broker){
        Broker res = brokerService.create(broker);
        return ResponseWrapperFactory.create(ResponseWrapper.SUCCESS, res);
    }

    @GetMapping("")
    public ResponseWrapper findAll(){
        List<Broker> res = brokerService.findAll();
        return ResponseWrapperFactory.create(ResponseWrapper.SUCCESS, res);
    }

    @DeleteMapping("/{brokerId}")
    public ResponseWrapper deleteById(@PathVariable Integer brokerId){
        boolean delete = brokerService.deleteById(brokerId);
        String status = delete ? ResponseWrapper.SUCCESS : ResponseWrapper.ERROR;
        String detail = delete ? "Delete Success" : "Delete Error";

        return ResponseWrapperFactory.create(status, detail);
    }
}
