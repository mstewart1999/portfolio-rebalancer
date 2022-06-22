package com.msfinance.pbalancer.model.rebalance;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.msfinance.pbalancer.model.ProfileSettings;

public class RebalancingToleranceBands implements IRebalancingMethod
{
    private double bandAbsolute = 0.05;
    private double bandRelative = 0.25;
    private BigDecimal rebalanceMinimumDollars = BigDecimal.ZERO;

    public RebalancingToleranceBands()
    {
    }

    public RebalancingToleranceBands(final ProfileSettings settings)
    {
        bandAbsolute = settings.getRebalanceToleranceBandAbsolute();
        bandRelative = settings.getRebalanceToleranceBandRelative();
        rebalanceMinimumDollars = settings.getRebalanceMinimumDollars();
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
            BigDecimal change = assetClassValue.subtract(maxValue).setScale(2, RoundingMode.HALF_UP).negate();
            if(change.doubleValue() <= -rebalanceMinimumDollars.doubleValue())
            {
                return change;
            }
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
                BigDecimal change = assetClassValue.subtract(targetValue).setScale(2, RoundingMode.HALF_UP).negate();
                if(change.doubleValue() <= -rebalanceMinimumDollars.doubleValue())
                {
                    return change;
                }
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
            BigDecimal change = minValue.subtract(assetClassValue).setScale(2, RoundingMode.HALF_UP);
            if(change.doubleValue() >= rebalanceMinimumDollars.doubleValue())
            {
                return change;
            }
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
                BigDecimal change = targetValue.subtract(assetClassValue).setScale(2, RoundingMode.HALF_UP);
                if(change.doubleValue() >= rebalanceMinimumDollars.doubleValue())
                {
                    return change;
                }
            }
        }
        return BigDecimal.ZERO;
    }

}
