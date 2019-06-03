package com.example.tradersocket.Domain.Entity;

import com.example.tradersocket.Domain.Entity.Util.Composite;

import java.util.ArrayList;
import java.util.List;

public class MarketDepth {
    private String id;
    private List<Composite> buyers;
    private List<Composite> sellers;
    private String marketDepthId;

    public MarketDepth() {
    }

    public String getMarketDepthId() {
        return marketDepthId;
    }

    public void setMarketDepthId(String marketDepthId) {
        this.marketDepthId = marketDepthId;
    }

    public List<Composite> getBuyers() {
        return buyers;
    }

    public void setBuyers(List<Composite> buyers) {
        this.buyers = buyers;
    }

    public List<Composite> getSellers() {
        return sellers;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSellers(List<Composite> sellers) {
        this.sellers = sellers;
    }
}