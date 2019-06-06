package com.example.tradersocket.Exception;

public class BrokerSocketNotExistException extends RuntimeException{
    public BrokerSocketNotExistException(String message){
        super(message);
    }
}
