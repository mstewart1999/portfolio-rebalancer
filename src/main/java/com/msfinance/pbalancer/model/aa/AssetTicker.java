package com.msfinance.pbalancer.model.aa;

public class AssetTicker
{
    private final String symbol;
    private final String name;

    public AssetTicker(final String symbol, final String name)
    {
        this.symbol = symbol;
        this.name = name;
    }

    public String getSymbol()
    {
        return symbol;
    }

    public String getName()
    {
        return name;
    }

}
