package com.pbalancer.client.model.rebalance;

import static com.pbalancer.client.model.rebalance.TransactionSpecific.Type.Buy;
import static com.pbalancer.client.model.rebalance.TransactionSpecific.Type.Sell;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pbalancer.client.StateManager;
import com.pbalancer.client.model.Account;
import com.pbalancer.client.model.AccountTaxType;
import com.pbalancer.client.model.Asset;
import com.pbalancer.client.model.Portfolio;
import com.pbalancer.client.model.Asset.PricingType;
import com.pbalancer.client.model.aa.AANode;
import com.pbalancer.client.model.aa.AANodeType;
import com.pbalancer.client.model.aa.AssetClass;
import com.pbalancer.client.model.aa.DoubleExpression;
import com.pbalancer.client.model.aa.PreferredAsset;
import com.pbalancer.client.util.Validation;

public class RebalanceManager
{
    private static final Logger LOG = LoggerFactory.getLogger(RebalanceManager.class);

    private static final double MIN_INVESTIBLE = TransactionSpecific.MIN_INVESTIBLE;
    private static final String ASSET_CLASS_MISC = "MISC";
    private static final String ASSET_NAME_MISC = "assets matching your asset allocation";
    private static final String ASSET_NAME_GENERIC = "this asset class";

    private static final int MAX_REBALANCE_ITERATIONS = 10;

    public static ActualAANode toActualAssetAllocation(final Portfolio portfolio)
    {
        BigDecimal total = portfolio.getLastValue();
        IRebalancingMethod method = new RebalancingToleranceBands(portfolio.getProfile().getSettings());
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


    /**
     * Determine which (if any) buy/sell transactions to perform across your accounts.
     * @param p the portfolio
     * @param rootAaan Consolidated view of target vs actual asset allocation
     */
    public static List<TransactionSpecific> toRebalanceSuggestions(final Portfolio p)
    {
        List<TransactionSpecific> suggestions = new ArrayList<>();
        Portfolio workingP = p.clone();
        workingP.setProfile(p.getProfile());

        int iterations = 0;
        while(iterations < MAX_REBALANCE_ITERATIONS)
        {
            ActualAANode rootAaan = RebalanceManager.toActualAssetAllocation(workingP);
            List<TransactionSpecific> thisRound = RebalanceManager.toRebalanceSuggestions(workingP, rootAaan);
            if(thisRound.size() == 0)
            {
                break;
            }
            suggestions.addAll(thisRound);
            workingP = workingP.apply(thisRound);
            iterations++;
        }

        if(iterations >= MAX_REBALANCE_ITERATIONS)
        {
            // TODO: what action?
        }

        suggestions = TransactionSpecific.consolidate(suggestions);

        return suggestions;
    }

    /**
     * Determine which (if any) buy/sell transactions to perform across your accounts.
     * @param p the portfolio
     * @param rootAaan Consolidated view of target vs actual asset allocation
     * @return a list of suggested rebalancing transactions
     */
    private static List<TransactionSpecific> toRebalanceSuggestions(final Portfolio p, final ActualAANode rootAaan)
    {
        List<ActualAANode> all = rootAaan.allLeaves();
        List<ActualAANode> sells = new ArrayList<>();
        List<ActualAANode> buys = new ArrayList<>();

        TempCash freedCashByAccountId = new TempCash();
        TempCash neededCashByAccountId = new TempCash();


        for(ActualAANode n : all)
        {
            if(n.getSellLow().doubleValue() <= -MIN_INVESTIBLE)
            {
                if(!n.getName().equals(ASSET_CLASS_MISC))
                {
                    // don't recommend to sell the thing this algorithm just recommended to buy
                    sells.add(n);
                }
            }
            if(n.getBuyLow().doubleValue() >= MIN_INVESTIBLE)
            {
                buys.add(n);
            }
        }
        if((sells.size() == 0) && (buys.size() == 0))
        {
            return Collections.emptyList();
        }

        List<TransactionSpecific> suggestions = new ArrayList<>();
        suggestions.addAll(toRebalanceSells(sells, freedCashByAccountId));
        // deal with cash changes caused by sells above
        suggestions.addAll(toInvestSuggestions(p, rootAaan, freedCashByAccountId));

        // only progress to buys once there are no further sells
        // avoid double buying when toInvestSuggestions() has overlap with toRebalanceBuys()
        // this method is meant to be called in iterations
        if(suggestions.size() == 0)
        {
            suggestions.addAll(toRebalanceBuys(buys, neededCashByAccountId));
            // deal with cash changes caused by buys above
            suggestions.addAll(toWithdrawalSuggestions(p, rootAaan, neededCashByAccountId));
        }

//        {
//            // offset negative/positive and resplit
//            TempCash merged = TempCash.sum(freedCashByAccountId, neededCashByAccountId);
//            freedCashByAccountId = merged.positiveOnly();
//            neededCashByAccountId = merged.negativeOnly();
//
//            // deal with cash changes caused by buy/sell above
//            suggestions.addAll(toInvestSuggestions(p, rootAaan, freedCashByAccountId));
//            suggestions.addAll(toWithdrawalSuggestions(p, rootAaan, neededCashByAccountId));
//        }

        return suggestions;
    }

    private static List<TransactionSpecific> toRebalanceSells(final List<ActualAANode> sells, final TempCash freedCashByAccountId)
    {
        List<TransactionSpecific> suggestions = new ArrayList<>();
        for(ActualAANode n : sells)
        {
            String assetClass = n.getName();
            List<Asset> divisible = filterByDivisible(n.getActual(), true);
            List<Asset> nonDivisible = filterByDivisible(n.getActual(), false);

            // target selling to midpoint of range (positive number here)
            double surplus = n.getSellHigh().add(n.getSellLow()).divide(BigDecimal.valueOf(2.0)).negate().doubleValue();

            if(divisible.size() > 0)
            {
                List<Asset> taxAdvantaged = filterByTaxAdvantagedAssets(divisible, true);
                List<Asset> nonTaxAdvantaged = filterByTaxAdvantagedAssets(divisible, false);

                // prioritize tax advantaged accounts for selling, to avoid capital gains
                for(Asset a : taxAdvantaged)
                {
                    if(surplus >= MIN_INVESTIBLE)
                    {
                        double howMuchDollars;
                        double howMuchUnits;
                        if(surplus >= a.getBestTotalValue().doubleValue())
                        {
                            howMuchDollars = a.getBestTotalValue().doubleValue();
                            surplus -= howMuchDollars;
                        }
                        else
                        {
                            howMuchDollars = surplus;
                            surplus = 0.0;
                        }
                        howMuchUnits = howMuchDollars / a.getBestUnitValue().doubleValue();
                        suggestions.add(new TransactionSpecific(Sell, assetClass, nameOf(a), -howMuchUnits, -howMuchDollars, a.getAccount(), null));
                        freedCashByAccountId.add(a.getAccount(), howMuchDollars);
                    }
                }
                // then non tax advantaged accounts
                for(Asset a : nonTaxAdvantaged)
                {
                    if(surplus >= MIN_INVESTIBLE)
                    {
                        double howMuchDollars;
                        double howMuchUnits;
                        if(surplus >= a.getBestTotalValue().doubleValue())
                        {
                            howMuchDollars = a.getBestTotalValue().doubleValue();
                            surplus -= howMuchDollars;
                        }
                        else
                        {
                            howMuchDollars = surplus;
                            surplus = 0.0;
                        }
                        howMuchUnits = howMuchDollars / a.getBestUnitValue().doubleValue();
                        suggestions.add(new TransactionSpecific(Sell, assetClass, nameOf(a), -howMuchUnits, -howMuchDollars, a.getAccount(), null));
                        freedCashByAccountId.add(a.getAccount(), howMuchDollars);
                    }
                }
            }
            if(nonDivisible.size() > 0)
            {
                // finally just report it to them
                for(Asset a : nonDivisible)
                {
                    if(surplus >= MIN_INVESTIBLE)
                    {
                        double howMuchDollars;
                        boolean wasAll;
                        if(surplus >= a.getBestTotalValue().doubleValue())
                        {
                            howMuchDollars = a.getBestTotalValue().doubleValue();
                            surplus -= howMuchDollars;
                            wasAll = true;
                        }
                        else
                        {
                            howMuchDollars = surplus;
                            surplus = 0.0;
                            wasAll = false;
                        }
                        if(wasAll)
                        {
                            suggestions.add(new TransactionSpecific(Sell, assetClass, nameOf(a), null, -howMuchDollars, a.getAccount(), "represents entire asset"));
                        }
                        else
                        {
                            suggestions.add(new TransactionSpecific(Sell, assetClass, nameOf(a), null, -howMuchDollars, a.getAccount(), "this asset is a single unit, so may not be partially sellable"));
                        }
                        freedCashByAccountId.add(a.getAccount(), howMuchDollars);
                    }
                }
            }
        }
        return suggestions;
    }

    private static List<TransactionSpecific> toRebalanceBuys(final List<ActualAANode> buys, final TempCash neededCashByAccountId)
    {
        List<TransactionSpecific> suggestions = new ArrayList<>();
        for(ActualAANode n : buys)
        {
            String assetClass = n.getName();
            List<Asset> divisible = filterByDivisible(n.getActual(), true);
            List<Asset> nonDivisible = filterByDivisible(n.getActual(), false);

            // target buying to midpoint of range (positive number here)
            double deficit = n.getBuyHigh().add(n.getBuyLow()).divide(BigDecimal.valueOf(2.0)).doubleValue();

            if(divisible.size() > 0)
            {
                List<Asset> taxAdvantaged = filterByTaxAdvantagedAssets(divisible, true);
                List<Asset> nonTaxAdvantaged = filterByTaxAdvantagedAssets(divisible, false);

                // prioritize tax advantaged accounts for buying, to avoid capital gains
                for(Asset a : taxAdvantaged)
                {
                    if(deficit >= MIN_INVESTIBLE)
                    {
                        double howMuchDollars;
                        double howMuchUnits;
                        if(deficit >= a.getBestTotalValue().doubleValue())
                        {
                            howMuchDollars = a.getBestTotalValue().doubleValue();
                            deficit -= howMuchDollars;
                        }
                        else
                        {
                            howMuchDollars = deficit;
                            deficit = 0.0;
                        }
                        howMuchUnits = howMuchDollars / a.getBestUnitValue().doubleValue();
                        suggestions.add(new TransactionSpecific(Buy, assetClass, nameOf(a), howMuchUnits, howMuchDollars, a.getAccount(), null));
                        neededCashByAccountId.subtract(a.getAccount(), howMuchDollars);
                    }
                }
                // then non tax advantaged accounts
                for(Asset a : nonTaxAdvantaged)
                {
                    if(deficit >= MIN_INVESTIBLE)
                    {
                        double howMuchDollars;
                        double howMuchUnits;
                        if(deficit >= a.getBestTotalValue().doubleValue())
                        {
                            howMuchDollars = a.getBestTotalValue().doubleValue();
                            deficit -= howMuchDollars;
                        }
                        else
                        {
                            howMuchDollars = deficit;
                            deficit = 0.0;
                        }
                        howMuchUnits = howMuchDollars / a.getBestUnitValue().doubleValue();
                        suggestions.add(new TransactionSpecific(Buy, assetClass, nameOf(a), howMuchUnits, howMuchDollars, a.getAccount(), null));
                        neededCashByAccountId.subtract(a.getAccount(), howMuchDollars);
                    }
                }
            }
            if(nonDivisible.size() > 0)
            {
                // finally just report it to them
                for(Asset a : nonDivisible)
                {
                    if(deficit >= MIN_INVESTIBLE)
                    {
                        double howMuchDollars;
                        boolean wasAll;
                        if(deficit >= a.getBestTotalValue().doubleValue())
                        {
                            howMuchDollars = a.getBestTotalValue().doubleValue();
                            deficit -= howMuchDollars;
                            wasAll = true;
                        }
                        else
                        {
                            howMuchDollars = deficit;
                            deficit = 0.0;
                            wasAll = false;
                        }
                        if(wasAll)
                        {
                            suggestions.add(new TransactionSpecific(Buy, assetClass, nameOf(a), null, howMuchDollars, a.getAccount(), "represents entire asset"));
                        }
                        else
                        {
                            suggestions.add(new TransactionSpecific(Buy, assetClass, nameOf(a), null, howMuchDollars, a.getAccount(), "this asset is a single unit, so may not be partially buyable"));
                        }

                        neededCashByAccountId.subtract(a.getAccount(), howMuchDollars);
                    }
                }
            }
        }
        return suggestions;
    }


    private static List<Asset> filterByDivisible(final List<Asset> actual, final boolean b)
    {
        // consider things priced per unit as rebalanceable
        // ex: real estate is hard to rebalance
        if(b)
        {
            return actual.stream()
                    .filter(a -> a.getPricingType() != PricingType.MANUAL_PER_WHOLE)
                    .toList();
        }
        else
        {
            return actual.stream()
                    .filter(a -> a.getPricingType() == PricingType.MANUAL_PER_WHOLE)
                    .toList();
        }
    }

    private static List<Asset> filterByTaxAdvantagedAssets(final Collection<Asset> actual, final boolean b)
    {
        List<Asset> found = actual.stream()
            .filter(a -> matchTaxAdvantaged(a.getAccount().getType().getType(), b))
            .toList();
        return found;
    }

    private static List<Account> filterByTaxAdvantagedAccounts(final Collection<Account> account, final boolean b)
    {
        List<Account> found = account.stream()
            .filter(a -> matchTaxAdvantaged(a.getType().getType(), b))
            .toList();
        return found;
    }

    private static boolean matchTaxAdvantaged(final AccountTaxType type, final boolean b)
    {
        if(b)
        {
            return type.isTaxAdvantaged();
        }
        else
        {
            return !type.isTaxAdvantaged();
        }
    }

    private static String nameOf(final Asset a)
    {
        if(!Validation.isBlank(a.getTicker()))
        {
            return a.getTicker();
        }
        return a.getBestName();
    }




    /**
     * Determine which (if any) sell transactions to perform across your accounts,
     * given a set of desired withdrawals in various accounts.
     * @param p the portfolio in question
     * @param rootAaan Consolidated view of target vs actual asset allocation
     * @param neededCashByAccountId how much cash should be withdrawn from each account
     * @return a list of suggested buy transactions
     */
    public static List<TransactionSpecific> toWithdrawalSuggestions(final Portfolio p, final ActualAANode rootAaan, final TempCash neededCashByAccountId)
    {
        List<TransactionSpecific> suggestions = new ArrayList<>();

        double cashNeeded = neededCashByAccountId.total();
        List<TransactionGeneral> trans = new ArrayList<>();
        double cashRemainder = toGeneralWithdrawalSuggestions(p, rootAaan, cashNeeded, trans);

        for(TransactionGeneral tran : trans)
        {
            String assetClass = tran.assetClass();
            // allocate
            List<Asset> taxAdvantaged = filterByTaxAdvantagedAssets(tran.possibleAssets(), true);
            List<Asset> nonTaxAdvantaged = filterByTaxAdvantagedAssets(tran.possibleAssets(), false);

            List<Asset> ordered = new ArrayList<>();
            String favoredAcctType;
            if(!AssetClass.isEquity(assetClass))
            {
                // favor selling non-equities in taxable acct
                ordered.addAll(nonTaxAdvantaged);
                ordered.addAll(taxAdvantaged);
                favoredAcctType = "taxable";
            }
            else
            {
                // favor selling equities in tax advantaged acct
                ordered.addAll(taxAdvantaged);
                ordered.addAll(nonTaxAdvantaged);
                favoredAcctType = "tax advantaged";
            }

            double desiredWithdrawalCash = tran.howMuchDollars();

            List<Asset> divisible = filterByDivisible(ordered, true);
            List<Asset> nonDivisible = filterByDivisible(ordered, false);

            for(Asset a : divisible)
            {
                if(neededCashByAccountId.hasCash(a.getAccount()) && (desiredWithdrawalCash <= -MIN_INVESTIBLE))
                {
                    double neededCash = neededCashByAccountId.getCash(a.getAccount());
                    double sellCash;
                    double sellUnits;
                    if(Math.abs(desiredWithdrawalCash) > Math.abs(neededCash))
                    {
                        sellCash = neededCash;
                        desiredWithdrawalCash -= sellCash;
                    }
                    else
                    {
                        sellCash = desiredWithdrawalCash;
                        desiredWithdrawalCash = 0.0;
                    }
                    sellUnits = sellCash / a.getBestUnitValue().doubleValue();
                    suggestions.add(new TransactionSpecific(Sell, assetClass, nameOf(a), sellUnits, sellCash, a.getAccount(), null));
                    neededCashByAccountId.subtract(a.getAccount(), sellCash);
                }
            }
            for(Asset a : nonDivisible)
            {
                if(neededCashByAccountId.hasCash(a.getAccount()) && (desiredWithdrawalCash <= -MIN_INVESTIBLE))
                {
                    double neededCash = neededCashByAccountId.getCash(a.getAccount());
                    double sellCash;
                    if(Math.abs(desiredWithdrawalCash) > Math.abs(neededCash))
                    {
                        sellCash = neededCash;
                        desiredWithdrawalCash -= sellCash;
                    }
                    else
                    {
                        sellCash = desiredWithdrawalCash;
                        desiredWithdrawalCash = 0.0;
                    }
                    suggestions.add(new TransactionSpecific(Sell, assetClass, nameOf(a), null, sellCash, a.getAccount(), "this asset is a single unit, so may not be partially sellable"));
                    neededCashByAccountId.subtract(a.getAccount(), sellCash);
                }
            }
            /* Unlike buying, you cannot just sell an asset you don't have.  We will NOT be suggesting short sales.
            if(desiredWithdrawalCash <= -MIN_INVESTIBLE)
            {
                List<Account> taxAdvantagedAccts = filterByTaxAdvantagedAccounts(neededCashByAccountId.getAccounts(), true);
                List<Account> nonTaxAdvantagedAccts = filterByTaxAdvantagedAccounts(neededCashByAccountId.getAccounts(), false);

                List<Account> ordered2 = new ArrayList<>();
                if(!AssetClass.isEquity(tran.assetClass))
                {
                    // favor selling non-equities in taxable acct
                    ordered2.addAll(nonTaxAdvantagedAccts);
                    ordered2.addAll(taxAdvantagedAccts);
                }
                else
                {
                    // favor selling equities in tax advantaged acct
                    ordered2.addAll(taxAdvantagedAccts);
                    ordered2.addAll(nonTaxAdvantagedAccts);
                }

                for(Account acct : ordered2)
                {
                    double neededCash = neededCashByAccountId.getCash(acct);
                    if((neededCash <= -MIN_INVESTIBLE) && (desiredWithdrawalCash <= -MIN_INVESTIBLE))
                    {
                        double sellCash;
                        if(Math.abs(desiredWithdrawalCash) > Math.abs(neededCash))
                        {
                            sellCash = neededCash;
                            desiredWithdrawalCash -= sellCash;
                        }
                        else
                        {
                            sellCash = desiredWithdrawalCash;
                            desiredWithdrawalCash = 0.0;
                        }
                        String what = ASSET_NAME_GENERIC;
                        suggestions.add(new TransactionSpecific(Sell, assetClass, what, null, sellCash, acct, null));
                        neededCashByAccountId.subtract(acct, sellCash);
                    }
                }
            }
            */
        }

        cashRemainder = neededCashByAccountId.total(); // ignore old value
        if(cashRemainder <= -MIN_INVESTIBLE)
        {
            for(Account acct : neededCashByAccountId.getAccounts())
            {
                double howMuchDollars = neededCashByAccountId.getCash(acct);
                if(howMuchDollars <= -MIN_INVESTIBLE)
                {
                    // TODO: specify: subtract in a balanced fashion
                    String assetClass = ASSET_CLASS_MISC;
                    String what = ASSET_NAME_MISC;
                    suggestions.add(new TransactionSpecific(Sell, assetClass, what, null, howMuchDollars, acct, null));
                    neededCashByAccountId.subtract(acct, howMuchDollars);
                }
                if(howMuchDollars > 0.0)
                {
                    LOG.warn("math error in RebalanceManager.toWithdrawalSuggestions(): too much withdrawn from an account: " + howMuchDollars);
                }
            }
        }
        cashRemainder = neededCashByAccountId.total(); // ignore old value
        if(!Validation.almostEqual(cashRemainder, 0.0, MIN_INVESTIBLE))
        {
            LOG.warn("math error in RebalanceManager.toWithdrawalSuggestions(): discrepancy: cashRemainder=" + cashRemainder);
        }

        return suggestions;
    }

    /**
     * Determine which (if any) buy transactions to perform across your accounts,
     * given a set of cash in various accounts.
     * @param p the portfolio in question
     * @param rootAaan Consolidated view of target vs actual asset allocation
     * @param newCashByAcctId how much cash is available in each account that should be invested
     * @return a list of suggested buy transactions
     */
    public static List<TransactionSpecific> toInvestSuggestions(final Portfolio p, final ActualAANode rootAaan, final TempCash newCashByAcctId)
    {
        List<TransactionSpecific> suggestions = new ArrayList<>();

        double cashAvailable = newCashByAcctId.total();
        List<TransactionGeneral> trans = new ArrayList<>();
        double cashRemainder = toGeneralInvestSuggestions(p, rootAaan, cashAvailable, trans);

        for(TransactionGeneral tran : trans)
        {
            String assetClass = tran.assetClass();
            // allocate
            List<Asset> taxAdvantaged = filterByTaxAdvantagedAssets(tran.possibleAssets(), true);
            List<Asset> nonTaxAdvantaged = filterByTaxAdvantagedAssets(tran.possibleAssets(), false);

            List<Asset> ordered = new ArrayList<>();
            String favoredAcctType;
            if(AssetClass.isEquity(assetClass))
            {
                // favor buying equities in taxable acct
                ordered.addAll(nonTaxAdvantaged);
                ordered.addAll(taxAdvantaged);
                favoredAcctType = "taxable";
            }
            else
            {
                // favor buying non-equities in tax advantaged acct
                ordered.addAll(taxAdvantaged);
                ordered.addAll(nonTaxAdvantaged);
                favoredAcctType = "tax advantaged";
            }

            double desiredInvestCash = tran.howMuchDollars();

            List<Asset> divisible = filterByDivisible(ordered, true);
            List<Asset> nonDivisible = filterByDivisible(ordered, false);

            for(Asset a : divisible)
            {
                if(newCashByAcctId.hasCash(a.getAccount()) && (desiredInvestCash >= MIN_INVESTIBLE))
                {
                    double availableCash = newCashByAcctId.getCash(a.getAccount());
                    double buyCash;
                    double buyUnits;
                    if(desiredInvestCash > availableCash)
                    {
                        buyCash = availableCash;
                        desiredInvestCash -= buyCash;
                    }
                    else
                    {
                        buyCash = desiredInvestCash;
                        desiredInvestCash = 0.0;
                    }
                    buyUnits = buyCash / a.getBestUnitValue().doubleValue();
                    suggestions.add(new TransactionSpecific(Buy, assetClass, nameOf(a), buyUnits, buyCash, a.getAccount(), null));
                    newCashByAcctId.subtract(a.getAccount(), buyCash);
                }
            }
            for(Asset a : nonDivisible)
            {
                if(newCashByAcctId.hasCash(a.getAccount()) && (desiredInvestCash >= MIN_INVESTIBLE))
                {
                    double availableCash = newCashByAcctId.getCash(a.getAccount());
                    double buyCash;
                    if(desiredInvestCash > availableCash)
                    {
                        buyCash = availableCash;
                        desiredInvestCash -= buyCash;
                    }
                    else
                    {
                        buyCash = desiredInvestCash;
                        desiredInvestCash = 0.0;
                    }
                    suggestions.add(new TransactionSpecific(Buy, assetClass, nameOf(a), null, buyCash, a.getAccount(), "this asset is a single unit, so may not be partially buyable"));
                    newCashByAcctId.subtract(a.getAccount(), buyCash);
                }
            }
            if(desiredInvestCash >= MIN_INVESTIBLE)
            {
                List<Account> taxAdvantagedAccts = filterByTaxAdvantagedAccounts(newCashByAcctId.getAccounts(), true);
                List<Account> nonTaxAdvantagedAccts = filterByTaxAdvantagedAccounts(newCashByAcctId.getAccounts(), false);

                List<Account> ordered2 = new ArrayList<>();
                if(AssetClass.isEquity(tran.assetClass()))
                {
                    // favor buying equities in taxable acct
                    ordered2.addAll(nonTaxAdvantagedAccts);
                    ordered2.addAll(taxAdvantagedAccts);
                }
                else
                {
                    // favor buying non-equities in tax advantaged acct
                    ordered2.addAll(taxAdvantagedAccts);
                    ordered2.addAll(nonTaxAdvantagedAccts);
                }

                for(Account acct : ordered2)
                {
                    double availableCash = newCashByAcctId.getCash(acct);
                    if((availableCash >= MIN_INVESTIBLE) && (desiredInvestCash >= MIN_INVESTIBLE))
                    {
                        double buyCash;
                        if(desiredInvestCash > availableCash)
                        {
                            buyCash = availableCash;
                            desiredInvestCash -= buyCash;
                        }
                        else
                        {
                            buyCash = desiredInvestCash;
                            desiredInvestCash = 0.0;
                        }
                        String what = ASSET_NAME_GENERIC;
                        PreferredAsset acm = p.lookupPreferredAsset(assetClass);
                        if((acm != null) && !Validation.isBlank(acm.getPrimaryAssetTicker()))
                        {
                            what = acm.getPrimaryAssetTicker();
                        }
                        suggestions.add(new TransactionSpecific(Buy, assetClass, what, null, buyCash, acct, null));
                        newCashByAcctId.subtract(acct, buyCash);
                    }
                }
            }
        }

        cashRemainder = newCashByAcctId.total(); // ignore old value
        if(cashRemainder >= MIN_INVESTIBLE)
        {
            for(Account acct : newCashByAcctId.getAccounts())
            {
                double howMuchDollars = newCashByAcctId.getCash(acct);
                if(howMuchDollars >= MIN_INVESTIBLE)
                {
                    // TODO: specify: add in a balanced fashion
                    String assetClass = ASSET_CLASS_MISC;
                    String what = ASSET_NAME_MISC;
                    suggestions.add(new TransactionSpecific(Buy, assetClass, what, null, howMuchDollars, acct, null));
                    newCashByAcctId.subtract(acct, howMuchDollars);
                }
                if(howMuchDollars < 0.0)
                {
                    LOG.warn("math error in RebalanceManager.toInvestSuggestions(): too much invested in an account: " + howMuchDollars);
                }
            }
        }
        cashRemainder = newCashByAcctId.total(); // ignore old value
        if(!Validation.almostEqual(cashRemainder, 0.0, MIN_INVESTIBLE))
        {
            LOG.warn("math error in RebalanceManager.toInvestSuggestions(): discrepancy: cashRemainder=" + cashRemainder);
        }

        return suggestions;
    }

    /**
     * Divide an amount of cash into "buys" in granularity of asset class.
     * @param p the portfolio
     * @param rootAaan a hierarchy or sub-hierarchy of asset allocation
     * @param newCash how much cash is available
     * @param buys an ugly "out" parameter for returning data
     * @return how much leftover cash there is after aligning all asset classes
     */
    private static double toGeneralInvestSuggestions(final Portfolio p, final ActualAANode rootAaan, final double newCash, final List<TransactionGeneral> buys)
    {
        double workingCash = newCash;
        List<ActualAANode> buyChildren = rootAaan.getChildren().stream()
                .filter(ac -> ac.getBuyToActual() > MIN_INVESTIBLE)
                .sorted((a,b) -> Double.compare(a.getBuyToActual(), b.getBuyToActual()))
                .toList();

        for(ActualAANode aaan : buyChildren)
        {
            if(aaan.isLeaf())
            {
                if(workingCash > MIN_INVESTIBLE)
                {
                    double howMuchNeeded = aaan.getBuyToActual();
                    double howMuch;
                    if(howMuchNeeded > workingCash)
                    {
                        howMuch = workingCash;
                        workingCash = 0.0;
                    }
                    else
                    {
                        howMuch = howMuchNeeded;
                        workingCash -= howMuch;
                    }
                    buys.add(new TransactionGeneral(aaan.getName(), aaan.getActual(), howMuch));
                }
            }
            else
            {
                // recurse on this portion of tree.  collect buys and decrement the cash
                workingCash = toGeneralInvestSuggestions(p, aaan, workingCash, buys);
            }
        }

        return workingCash;
    }

    /**
     * Generate an amount of cash from "sells" in granularity of asset class.
     * @param p the portfolio
     * @param rootAaan a hierarchy or sub-hierarchy of asset allocation
     * @param cash how much cash is desired (negative)
     * @param sells an ugly "out" parameter for returning data
     * @return how much cash is still needed after aligning all asset classes
     */
    private static double toGeneralWithdrawalSuggestions(final Portfolio p, final ActualAANode rootAaan, final double cash, final List<TransactionGeneral> sells)
    {
        double workingCash = cash;
        List<ActualAANode> sellChildren = rootAaan.getChildren().stream()
                .filter(ac -> ac.getSellToActual() < -MIN_INVESTIBLE)
                .sorted((a,b) -> Double.compare(-a.getSellToActual(), -b.getSellToActual()))
                .toList();

        for(ActualAANode aaan : sellChildren)
        {
            if(aaan.isLeaf())
            {
                if(workingCash < -MIN_INVESTIBLE)
                {
                    double howMuchNeeded = aaan.getSellToActual();
                    double howMuch;
                    if(Math.abs(howMuchNeeded) > Math.abs(workingCash))
                    {
                        howMuch = workingCash;
                        workingCash = 0.0;
                    }
                    else
                    {
                        howMuch = howMuchNeeded;
                        workingCash -= howMuch;
                    }
                    sells.add(new TransactionGeneral(aaan.getName(), aaan.getActual(), howMuch));
                }
            }
            else
            {
                // recurse on this portion of tree.  collect sells and decrement the cash
                workingCash = toGeneralWithdrawalSuggestions(p, aaan, workingCash, sells);
            }
        }

        return workingCash;
    }


}
