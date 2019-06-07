package com.example.tradersocket.Domain.Wrapper;


import com.example.tradersocket.Domain.Entity.Broker;

import javax.websocket.Session;

public class SessionWrapper {

    private Session session;
    private String sid;

    private Broker broker;
    private String marketDepthId;
    private boolean login = false;

    public SessionWrapper(){

    }

    public SessionWrapper(Session session, String sid){
        this.session = session;
        this.sid = sid;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public Broker getBroker() {
        return broker;
    }

    public void setBroker(Broker broker) {
        this.broker = broker;
    }

    public String getMarketDepthId() {
        return marketDepthId;
    }

    public void setMarketDepthId(String marketDepthId) {
        this.marketDepthId = marketDepthId;
    }

    public boolean isLogin() {
        return login;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }
}
