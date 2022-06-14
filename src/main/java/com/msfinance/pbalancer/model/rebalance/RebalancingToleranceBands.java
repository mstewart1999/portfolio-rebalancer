package com.msfinance.pbalancer.model.rebalance;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.msfinance.pbalancer.model.ProfileSettings;

public class RebalancingToleranceBands implements IRebalancingMethod
{
    private double bandAbsolute = 0.05;
    private double bandRelative = 0.25;

    public RebalancingToleranceBands()
    {
    }

    public RebalancingToleranceBands(final ProfileSettings settings)
    {
        bandAbsolute = settings.getRebalanceToleranceBandAbsolute();
        bandRelative = settings.getRebalanceToleranceBandRelative();
    }

    @Override
    public BigDecimal computeSellLow(final double targetPercent, final BigDecimal portfolioValue, final BigDecimal assetClassValue)
    {
        double maxRelative = targetPercent*(1+bandRelative);
        double maxAbsolute = targetPercent+bandAbsolute;
        double maxPercent = Math.min(Math.min(maxRelative, maxAbsolute), 1.0);

        BigDecimal maxValue = portfolioValue.multiply(new BigDecimal(maxPercent));
        if(assetClassValue.doubleValue() > maxValue.doubleValue())
        {
            return assetClassValue.subtract(maxValue).setScale(2, RoundingMode.HALF_UP).negate();
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal computeSellHigh(final double targetPercent, final BigDecimal portfolioValue, final BigDecimal assetClassValue)
    {
        if(computeSellLow(targetPercent, portfolioValue, assetClassValue).doubleValue() < 0.0)
        {
            BigDecimal targetValue = portfolioValue.multiply(new BigDecimal(targetPercent));
            if(assetClassValue.doubleValue() > targetValue.doubleValue())
            {
                return assetClassValue.subtract(targetValue).setScale(2, RoundingMode.HALF_UP).negate();
            }
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal computeBuyLow(final double targetPercent, final BigDecimal portfolioValue, final BigDecimal assetClassValue)
    {
        double minRelative = targetPercent*(1-bandRelative);
        double minAbsolute = targetPercent-bandAbsolute;
        double minPercent = Math.max(Math.max(minRelative, minAbsolute), 0.0);

        BigDecimal minValue = portfolioValue.multiply(new BigDecimal(minPercent));
        if(assetClassValue.doubleValue() < minValue.doubleValue())
        {
            return minValue.subtract(assetClassValue).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal computeBuyHigh(final double targetPercent, final BigDecimal portfolioValue, final BigDecimal assetClassValue)
    {
        if(computeBuyLow(targetPercent, portfolioValue, assetClassValue).doubleValue() > 0.0)
        {
            BigDecimal targetValue = portfolioValue.multiply(new BigDecimal(targetPercent));
            if(assetClassValue.doubleValue() < targetValue.doubleValue())
            {
                return targetValue.subtract(assetClassValue).setScale(2, RoundingMode.HALF_UP);
            }
        }
        return BigDecimal.ZERO;
    }

}
