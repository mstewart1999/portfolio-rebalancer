package com.msfinance.pbalancer.model.rebalance;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.msfinance.pbalancer.StateManager;
import com.msfinance.pbalancer.model.Asset;
import com.msfinance.pbalancer.model.Portfolio;
import com.msfinance.pbalancer.model.aa.AANode;
import com.msfinance.pbalancer.model.aa.AANodeType;
import com.msfinance.pbalancer.model.aa.AssetClass;
import com.msfinance.pbalancer.model.aa.DoubleExpression;
import com.msfinance.pbalancer.util.Validation;

public class RebalanceManager
{
    public static ActualAANode toActualAssetAllocation(final Portfolio portfolio)
    {
        BigDecimal total = portfolio.getLastValue();
        IRebalancingMethod method = new RebalancingBands525();
        ActualAANode rootAaan = RebalanceManager.wrap(portfolio.getTargetAA().getRoot(), total, method);
        ActualAANode unallocatedCategoryAaan = new ActualAANode(new AANode("N/A", "Unallocated", "Unallocated", 0, DoubleExpression.createSafe0Percent(), AANodeType.G), total, method);
        Map<String,ActualAANode> index = RebalanceManager.indexLeaves(rootAaan);


        Collection<Asset> assets = StateManager.listAssets(portfolio);
        for(Asset a : assets)
        {
            String ac = a.getAssetClass();
            if(Validation.isBlank(ac))
            {
                ac = AssetClass.UNDEFINED;
            }
            if(!index.containsKey(ac))
            {
                ActualAANode aaan = new ActualAANode(new AANode("N/A", ac, ac, 0, DoubleExpression.createSafe0Percent(), AANodeType.AC), total, method);
                index.put(ac, aaan);
                // put in unallocated category
                unallocatedCategoryAaan.getChildren().add(aaan);
            }
            index.get(ac).getActual().add(a);
        }
        if(unallocatedCategoryAaan.getChildren().size() > 0)
        {
            // put unallocated category in tree
            rootAaan.getChildren().add(unallocatedCategoryAaan);
        }

        return rootAaan;
    }

    private static ActualAANode wrap(final AANode taan, final BigDecimal portfolioValue, final IRebalancingMethod method)
    {
        ActualAANode aaan = new ActualAANode(taan, portfolioValue, method);
        for(AANode c : taan.children())
        {
            aaan.getChildren().add( wrap(c, portfolioValue, method) );
        }
        return aaan;
    }

    private static Map<String,ActualAANode> indexLeaves(final ActualAANode aaan)
    {
        if(aaan.isLeaf())
        {
            return Collections.singletonMap(aaan.getName(), aaan);
        }

        Map<String,ActualAANode> index = new HashMap<>();
        for(ActualAANode caaan : aaan.getChildren())
        {
            index.putAll(indexLeaves(caaan));
            // TODO: detect this somewhere
//            else
//            {
//                LOG.error("Asset Class is in target asset allocation twice: " + ac); // TODO
//            }
        }

        return index;
    }


    public static void dumpToConsole(final ActualAANode aaan)
    {
        System.out.printf("%13s: %%=%3.0f $%,12.2f Sell=[%,13.2f ; %,13.2f] Buy=[%,12.2f ; %,12.2f] %n",
                aaan.getName(),
                aaan.getTargetPercentOfPortfolio()*100,
                aaan.getTotalValue(),
                aaan.getSellHigh(),
                aaan.getSellLow(),
                aaan.getBuyLow(),
                aaan.getBuyHigh()
                );

        for(ActualAANode c : aaan.getChildren())
        {
            dumpToConsole(c);
        }
    }
}
