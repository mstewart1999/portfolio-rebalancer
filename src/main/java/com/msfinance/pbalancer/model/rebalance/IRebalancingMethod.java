package com.msfinance.pbalancer.model.rebalance;

import java.math.BigDecimal;

public interface IRebalancingMethod
{
    BigDecimal computeSellLow(double targetPercent, BigDecimal portfolioValue, BigDecimal assetClassValue);
    BigDecimal computeSellHigh(double targetPercent, BigDecimal portfolioValue, BigDecimal assetClassValue);

    BigDecimal computeBuyLow(double targetPercent, BigDecimal portfolioValue, BigDecimal assetClassValue);
    BigDecimal computeBuyHigh(double targetPercent, BigDecimal portfolioValue, BigDecimal assetClassValue);
}
