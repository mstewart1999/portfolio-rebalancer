package com.msfinance.pbalancer.model.rebalance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.msfinance.pbalancer.StateManager;
import com.msfinance.pbalancer.model.Account;
import com.msfinance.pbalancer.model.AccountTaxType;
import com.msfinance.pbalancer.model.Asset;
import com.msfinance.pbalancer.model.Asset.PricingType;
import com.msfinance.pbalancer.model.Portfolio;
import com.msfinance.pbalancer.model.aa.AANode;
import com.msfinance.pbalancer.model.aa.AANodeType;
import com.msfinance.pbalancer.model.aa.AssetClass;
import com.msfinance.pbalancer.model.aa.DoubleExpression;
import com.msfinance.pbalancer.util.Validation;

public class RebalanceManager
{
    private static final Logger LOG = LoggerFactory.getLogger(RebalanceManager.class);

    private static final double MIN_INVESTIBLE = 0.005;

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


    /**
     * Determine which (if any) buy/sell transactions to perform across your accounts.
     * @param p the portfolio
     * @param rootAaan Consolidated view of target vs actual asset allocation
     * @return a list of suggested rebalancing transactions
     */
    public static List<String> toRebalanceSuggestions(final Portfolio p, final ActualAANode rootAaan)
    {
        List<ActualAANode> all = rootAaan.allLeaves();
        List<ActualAANode> sells = new ArrayList<>();
        List<ActualAANode> buys = new ArrayList<>();

        Map<Account,Double> freedCashByAccountId = new HashMap<>();
        Map<Account,Double> neededCashByAccountId = new HashMap<>();


        for(ActualAANode n : all)
        {
            if(n.getSellLow().doubleValue() < 0)
            {
                sells.add(n);
            }
            if(n.getBuyLow().doubleValue() > 0)
            {
                buys.add(n);
            }
        }
        if((sells.size() == 0) && (buys.size() == 0))
        {
            return Collections.singletonList("No rebalancing suggested at this time");
        }

        List<String> suggestions = new ArrayList<>();
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
                    if(surplus > MIN_INVESTIBLE)
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
                        String what = nameOf(a);
                        String where = a.getAccount().getName();
                        suggestions.add(String.format("[%s] Sell %,.3f shares of %s in account '%s'", assetClass, howMuchUnits, what, where));
                        addFreeCash(freedCashByAccountId, a.getAccount(), howMuchDollars);
                    }
                }
                // then non tax advantaged accounts
                for(Asset a : nonTaxAdvantaged)
                {
                    if(surplus > MIN_INVESTIBLE)
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
                        String what = nameOf(a);
                        String where = a.getAccount().getName();
                        suggestions.add(String.format("[%s] Sell %,.3f shares of %s in account '%s'", assetClass, howMuchUnits, what, where));
                        addFreeCash(freedCashByAccountId, a.getAccount(), howMuchDollars);
                    }
                }
            }
            if(nonDivisible.size() > 0)
            {
                // finally just report it to them
                for(Asset a : nonDivisible)
                {
                    if(surplus > MIN_INVESTIBLE)
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
                        String what = nameOf(a);
                        String where = a.getAccount().getName();
                        if(wasAll)
                        {
                            suggestions.add(String.format("[%s] Sell %,.2f dollars of %s in account '%s' (this asset is a single unit, so may not be partially sellable)", assetClass, howMuchDollars, what, where));
                        }
                        else
                        {
                            suggestions.add(String.format("[%s] Sell %,.2f dollars of %s in account '%s' (represents entire asset))", assetClass, howMuchDollars, what, where));
                        }
                        addFreeCash(freedCashByAccountId, a.getAccount(), howMuchDollars);
                    }
                }
            }
        }
        suggestions.add("");

        for(ActualAANode n : buys)
        {
            String assetClass = n.getName();
            List<Asset> divisible = filterByDivisible(n.getActual(), true);
            List<Asset> nonDivisible = filterByDivisible(n.getActual(), false);

            // target buying to midpoint of range (positive number here)
            double deficit = n.getBuyHigh().add(n.getBuyLow()).divide(BigDecimal.valueOf(2.0)).doubleValue();

            // TODO: technically we can only buy in accts that have cash! or else we need to sell more stuff first
            if(divisible.size() > 0)
            {
                List<Asset> taxAdvantaged = filterByTaxAdvantagedAssets(divisible, true);
                List<Asset> nonTaxAdvantaged = filterByTaxAdvantagedAssets(divisible, false);

                // TODO: this prioritization doesn't make sense for buying
                // prioritize tax advantaged accounts for buying, to avoid capital gains
                for(Asset a : taxAdvantaged)
                {
                    if(deficit > MIN_INVESTIBLE)
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
                        String what = nameOf(a);
                        String where = a.getAccount().getName();
                        suggestions.add(String.format("[%s] Buy %,.3f shares of %s in account '%s'", assetClass, howMuchUnits, what, where));
                        subtractFreeCash(neededCashByAccountId, a.getAccount(), howMuchDollars);
                    }
                }
                // then non tax advantaged accounts
                for(Asset a : nonTaxAdvantaged)
                {
                    if(deficit > MIN_INVESTIBLE)
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
                        String what = nameOf(a);
                        String where = a.getAccount().getName();
                        suggestions.add(String.format("[%s] Buy %,.3f shares of %s in account '%s'", assetClass, howMuchUnits, what, where));
                        subtractFreeCash(neededCashByAccountId, a.getAccount(), howMuchDollars);
                    }
                }
            }
            if(nonDivisible.size() > 0)
            {
                // finally just report it to them
                for(Asset a : nonDivisible)
                {
                    if(deficit > MIN_INVESTIBLE)
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
                        String what = nameOf(a);
                        String where = a.getAccount().getName();
                        if(wasAll)
                        {
                            suggestions.add(String.format("[%s] Buy %,.2f dollars of %s in account '%s' (this asset is a single unit, so may not be partially sellable)", assetClass, howMuchDollars, what, where));
                        }
                        else
                        {
                            suggestions.add(String.format("[%s] Buy %,.2f dollars of %s in account '%s' (represents entire asset))", assetClass, howMuchDollars, what, where));
                        }
                        subtractFreeCash(neededCashByAccountId, a.getAccount(), howMuchDollars);
                    }
                }
            }
        }
        suggestions.add("");

        {
            suggestions.addAll(toWithdrawalSuggestions(p, rootAaan, neededCashByAccountId));
            suggestions.add("");
            suggestions.addAll(toInvestSuggestions(p, rootAaan, freedCashByAccountId));
            suggestions.add("");
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
        return "'" + a.getBestName() + "'";
    }


    private static void addFreeCash(final Map<Account,Double> freedCashByAccountId, final Account account, final double howMuchDollars)
    {
        if(freedCashByAccountId.containsKey(account))
        {
            freedCashByAccountId.put(account, freedCashByAccountId.get(account) + howMuchDollars);
        }
        else
        {
            freedCashByAccountId.put(account, howMuchDollars);
        }
    }

    private static void subtractFreeCash(final Map<Account,Double> freedCashByAccountId, final Account account, final double howMuchDollars)
    {
        if(freedCashByAccountId.containsKey(account))
        {
            freedCashByAccountId.put(account, freedCashByAccountId.get(account) - howMuchDollars);
        }
        else
        {
            freedCashByAccountId.put(account, -howMuchDollars);
        }
    }



    /**
     * Determine which (if any) sell transactions to perform across your accounts,
     * given a set of cash in various accounts
     * @param p the portfolio in question
     * @param rootAaan Consolidated view of target vs actual asset allocation
     * @param newCashByAcctId how much cash is available in each account that should be withdrawn
     * @return a list of suggested buy transactions
     */
    public static List<String> toWithdrawalSuggestions(final Portfolio p, final ActualAANode rootAaan, final Map<Account,Double> newCashByAcctId)
    {
        List<String> suggestions = new ArrayList<>();

        // TODO: implement
        suggestions.add("dummy withdrawal section: " + newCashByAcctId);

        return suggestions;
    }

    /**
     * Determine which (if any) buy transactions to perform across your accounts,
     * given a set of cash in various accounts
     * @param p the portfolio in question
     * @param rootAaan Consolidated view of target vs actual asset allocation
     * @param newCashByAcctId how much cash is available in each account that should be invested
     * @return a list of suggested buy transactions
     */
    public static List<String> toInvestSuggestions(final Portfolio p, final ActualAANode rootAaan, final Map<Account,Double> newCashByAcctId)
    {
        List<String> suggestions = new ArrayList<>();

        double cashAvailable = newCashByAcctId.values().stream().reduce((a,b) -> (a == null) ? b : a+b).orElse(0.0);
        List<BuyDesired> buys = new ArrayList<>();
        double cashRemainder = toGeneralInvestSuggestions(p, rootAaan, cashAvailable, buys);

        for(BuyDesired buy : buys)
        {
            String assetClass = buy.assetClass;
            // allocate
            List<Asset> taxAdvantaged = filterByTaxAdvantagedAssets(buy.possibleAssets, true);
            List<Asset> nonTaxAdvantaged = filterByTaxAdvantagedAssets(buy.possibleAssets, false);

            List<Asset> ordered = new ArrayList<>();
            String favoredAcctType;
            if(AssetClass.isEquity(assetClass))
            {
                // favor equities in taxable acct
                ordered.addAll(nonTaxAdvantaged);
                ordered.addAll(taxAdvantaged);
                favoredAcctType = "taxable";
            }
            else
            {
                // favor non-equities in tax advantaged acct
                ordered.addAll(taxAdvantaged);
                ordered.addAll(nonTaxAdvantaged);
                favoredAcctType = "tax advantaged";
            }

            double desiredInvestCash = buy.howMuchDollars;

            List<Asset> divisible = filterByDivisible(ordered, true);
            List<Asset> nonDivisible = filterByDivisible(ordered, false);

            for(Asset a : divisible)
            {
                if(newCashByAcctId.containsKey(a.getAccount()) && (desiredInvestCash > MIN_INVESTIBLE))
                {
                    double availableCash = newCashByAcctId.get(a.getAccount());
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
                    String what = nameOf(a);
                    String where = a.getAccount().getName();
                    suggestions.add(String.format("[%s] Buy %,.3f shares of %s in account '%s'", assetClass, buyUnits, what, where));
                    subtractFreeCash(newCashByAcctId, a.getAccount(), buyCash);
                }
            }
            for(Asset a : nonDivisible)
            {
                if(newCashByAcctId.containsKey(a.getAccount()) && (desiredInvestCash > MIN_INVESTIBLE))
                {
                    double availableCash = newCashByAcctId.get(a.getAccount());
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
                    String what = nameOf(a);
                    String where = a.getAccount().getName();
                    suggestions.add(String.format("[%s] Buy %,.2f dollars of %s in account '%s' (this asset is a single unit, so may not be partially buyable)", assetClass, buyCash, what, where));
                    subtractFreeCash(newCashByAcctId, a.getAccount(), buyCash);
                }
            }
            if(desiredInvestCash >= MIN_INVESTIBLE)
            {
                List<Account> taxAdvantagedAccts = filterByTaxAdvantagedAccounts(newCashByAcctId.keySet(), true);
                List<Account> nonTaxAdvantagedAccts = filterByTaxAdvantagedAccounts(newCashByAcctId.keySet(), false);

                List<Account> ordered2 = new ArrayList<>();
                if(AssetClass.isEquity(buy.assetClass))
                {
                    // favor equities in taxable acct
                    ordered2.addAll(nonTaxAdvantagedAccts);
                    ordered2.addAll(taxAdvantagedAccts);
                }
                else
                {
                    // favor non-equities in tax advantaged acct
                    ordered2.addAll(taxAdvantagedAccts);
                    ordered2.addAll(nonTaxAdvantagedAccts);
                }

                for(Account acct : ordered2)
                {
                    double availableCash = newCashByAcctId.get(acct);
                    if((availableCash > MIN_INVESTIBLE) && (desiredInvestCash > MIN_INVESTIBLE))
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
                        String what = "this asset class";
                        String where = acct.getName();
                        suggestions.add(String.format("[%s] Buy %,.2f dollars of %s in account '%s'", assetClass, buyCash, what, where));
                        subtractFreeCash(newCashByAcctId, acct, buyCash);
                    }
                }
            }
        }

        if(cashRemainder > MIN_INVESTIBLE)
        {
            suggestions.add("");
            double cashInvested = 0.0;
            for(Account acct : newCashByAcctId.keySet())
            {
                double howMuchDollars = newCashByAcctId.get(acct);
                String where = acct.getName();
                if(howMuchDollars > MIN_INVESTIBLE)
                {
                    // TODO: specify: add in a balanced fashion
                    suggestions.add(String.format("[Misc] Buy %,.2f dollars in percentages matching your asset allocation in account '%s'", howMuchDollars, where));
                    cashInvested += howMuchDollars;
                }
                if(howMuchDollars < 0.0)
                {
                    LOG.warn("math error in RebalanceManager.toInvestSuggestions(): too much invested in an account: " + howMuchDollars);
                }
            }
            if(!Validation.almostEqual(cashRemainder, cashInvested, 1.00))
            {
                LOG.warn("math error in RebalanceManager.toInvestSuggestions(): discrepancy: " + cashAvailable + " vs " + cashInvested);
            }
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
    private static double toGeneralInvestSuggestions(final Portfolio p, final ActualAANode rootAaan, final double newCash, final List<BuyDesired> buys)
    {
        double workingCash = newCash;
        List<ActualAANode> buyChildren = rootAaan.getChildren().stream()
                .filter(ac -> ac.getBuyToActual() > 0.0)
                .sorted((a,b) -> Double.compare(a.getBuyToActual(), b.getBuyToActual()))
                .toList();

        for(ActualAANode aaan : buyChildren)
        {
            if(aaan.isLeaf())
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
                buys.add(new BuyDesired(aaan.getName(), aaan.getActual(), howMuch));
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
     * Temporary placeholder data for invest suggestion algorithm.
     */
    final record BuyDesired(String assetClass, List<Asset> possibleAssets, double howMuchDollars)
    {
    }

}
