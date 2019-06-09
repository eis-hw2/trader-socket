package com.example.tradersocket.Core.BrokerSocket;

import com.example.tradersocket.Domain.Entity.MarketDepth;
import com.example.tradersocket.Domain.Entity.MarketQuotation;

public class DataPair {
    private MarketQuotation marketQuotation;
    private MarketDepth marketDepth;
    private int curVolume;
    private long timestamp;

    public MarketQuotation getMarketQuotation() {
        return marketQuotation;
    }

    public void setMarketQuotation(MarketQuotation marketQuotation) {
        this.marketQuotation = marketQuotation;
    }

    public MarketDepth getMarketDepth() {
        return marketDepth;
    }

    public void setMarketDepth(MarketDepth marketDepth) {
        this.marketDepth = marketDepth;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getCurVolume() {
        return curVolume;
    }

    public void setCurVolume(int curVolume) {
        this.curVolume = curVolume;
    }
}
