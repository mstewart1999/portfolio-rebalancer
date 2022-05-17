package com.msfinance.pbalancer.model.rebalance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

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
        this.portfolioValue = portfolioValue;
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
        return computeTotalValue().divide(portfolioValue, 4, RoundingMode.HALF_UP).doubleValue();
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
