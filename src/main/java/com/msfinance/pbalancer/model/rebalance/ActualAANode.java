package com.msfinance.pbalancer.model.rebalance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.msfinance.pbalancer.model.Asset;
import com.msfinance.pbalancer.model.aa.AANode;

public class ActualAANode
{
    private final AANode target;
    private final BigDecimal portfolioValue;
    private final IRebalancingMethod method;
    private final List<Asset> actual = new ArrayList<>();

    private final List<ActualAANode> children = new ArrayList<>();
    //private ActualAANode parent;


    public ActualAANode(final AANode target, final BigDecimal portfolioValue, final IRebalancingMethod method)
    {
        this.target = target;
        this.portfolioValue = Objects.requireNonNullElse(portfolioValue, BigDecimal.ZERO);
        this.method = method;
    }

    /*
    public AANode getTarget()
    {
        return target;
    }

    public IRebalancingMethod getMethod()
    {
        return method;
    }
    */

    public List<Asset> getActual()
    {
        return actual;
    }

    public List<ActualAANode> getChildren()
    {
        return children;
    }


    public String getPath()
    {
        return target.getPath();
    }

    public String getName()
    {
        return target.getName();
    }

    public BigDecimal getTotalValue()
    {
        return computeTotalValue();
    }

    public double getTargetPercentOfPortfolio()
    {
        return target.getPercentOfRoot();
    }

    public double getActualPercentOfPortfolio()
    {
        if(portfolioValue.equals(BigDecimal.ZERO))
        {
            return 0.0;
        }
        return computeTotalValue().divide(portfolioValue, 4, RoundingMode.HALF_UP).doubleValue();
    }

    public double getAbsoluteDifferencePercent()
    {
        // negative (sell) if actual is bigger than target
        return getTargetPercentOfPortfolio() - getActualPercentOfPortfolio();
    }

    public BigDecimal getSellLow()
    {
        return method.computeSellLow(getTargetPercentOfPortfolio(), portfolioValue, computeTotalValue());
    }
    public BigDecimal getSellHigh()
    {
        return method.computeSellHigh(getTargetPercentOfPortfolio(), portfolioValue, computeTotalValue());
    }

    public BigDecimal getBuyLow()
    {
        return method.computeBuyLow(getTargetPercentOfPortfolio(), portfolioValue, computeTotalValue());
    }
    public BigDecimal getBuyHigh()
    {
        return method.computeBuyHigh(getTargetPercentOfPortfolio(), portfolioValue, computeTotalValue());
    }

    public double getBuyToActual()
    {
        double absDiffPercent = getAbsoluteDifferencePercent();
        if(absDiffPercent > 0.0)
        {
            return portfolioValue.doubleValue()*absDiffPercent;
        }
        return 0.0;
    }

    public double getSellToActual()
    {
        double absDiffPercent = getAbsoluteDifferencePercent();
        if(absDiffPercent < 0.0)
        {
            return portfolioValue.doubleValue()*absDiffPercent;
        }
        return 0.0;
    }



    private BigDecimal computeTotalValue()
    {
        BigDecimal assetClassValue = BigDecimal.ZERO;

        if(target.isLeaf())
        {
            for(Asset a : actual)
            {
                if(a.getBestTotalValue() != null)
                {
                    assetClassValue = assetClassValue.add(a.getBestTotalValue());
                }
            }
        }
        else
        {
            for(ActualAANode child : children)
            {
                assetClassValue = assetClassValue.add(child.computeTotalValue());
            }
        }
        return assetClassValue;
    }

    public List<ActualAANode> allLeaves()
    {
        List<ActualAANode> all = new ArrayList<>();
        if(this.isLeaf())
        {
            all.add(this);
        }
        for(ActualAANode child : children)
        {
            all.addAll(child.allLeaves());
        }
        return all;
    }

    public boolean isLeaf()
    {
        return target.isLeaf();
    }


}
