package com.example.tradersocket.Domain.Entity.Util;

import com.example.tradersocket.Domain.Entity.MarketQuotation;
import com.example.tradersocket.Domain.Entity.MarketDepth;

public class SocketMessage {
    private MarketQuotation marketQuotation;
    private MarketDepth marketDepth;

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
}
