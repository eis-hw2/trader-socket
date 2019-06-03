package com.example.tradersocket.Domain.Entity;

public class MarketQuotation {
    private float lastClosePrice;
    private float openPrice;
    private float closePrice;
    private float highPrice;
    private float lowPrice;
    private float currentPrice;
    private float changePrice;
    private float changePercent;
    private int totalVolume;
    private int totalShare;
    private float turnoverRate;
    private String date;
    private String marketDepthId;
    private String id;

    public MarketQuotation() {
    }

    public float getLastClosePrice() {
        return lastClosePrice;
    }

    public void setLastClosePrice(float lastClosePrice) {
        this.lastClosePrice = lastClosePrice;
    }

    public float getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(float openPrice) {
        this.openPrice = openPrice;
    }

    public float getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(float closePrice) {
        this.closePrice = closePrice;
    }

    public float getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(float highPrice) {
        this.highPrice = highPrice;
    }

    public float getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(float lowPrice) {
        this.lowPrice = lowPrice;
    }

    public float getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(float currentPrice) {
        this.currentPrice = currentPrice;
    }

    public float getChangePrice() {
        return changePrice;
    }

    public void setChangePrice(float changePrice) {
        this.changePrice = changePrice;
    }

    public float getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(float changePercent) {
        this.changePercent = changePercent;
    }

    public int getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(int totalVolume) {
        this.totalVolume = totalVolume;
    }

    public int getTotalShare() {
        return totalShare;
    }

    public void setTotalShare(int totalShare) {
        this.totalShare = totalShare;
    }

    public float getTurnoverRate() {
        return turnoverRate;
    }

    public void setTurnoverRate(float turnoverRate) {
        this.turnoverRate = turnoverRate;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMarketDepthId() {
        return marketDepthId;
    }

    public void setMarketDepthId(String marketDepthId) {
        this.marketDepthId = marketDepthId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
