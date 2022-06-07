package com.msfinance.pbalancer.model.rebalance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.msfinance.pbalancer.model.Account;
import com.msfinance.pbalancer.util.Validation;

public record TransactionSpecific(
        String assetClass,
        String whatAsset, // either a ticker, or a asset name
        Double howMuchUnits,
        double howMuchDollars,
        Account where,
        String note
        )
{

    @Override
    public String toString()
    {
        String type = (howMuchDollars > 0) ? "Buy" : "Sell";
        String whatPretty = whatAsset.contains(" ") ? "'" + whatAsset + "'" : whatAsset;
        double sign = (howMuchDollars > 0) ? 1.0 : -1.0;

        String str;
        if(howMuchUnits != null)
        {
            str = String.format("[%s] %s %,.3f shares ($%,.2f) of %s in account '%s'", assetClass, type, sign*howMuchUnits, sign*howMuchDollars, whatPretty, where.getName());
        }
        else
        {
            str = String.format("[%s] %s $%,.2f of %s in account '%s'", assetClass, type, sign*howMuchDollars, whatPretty, where.getName());
        }

        if(!Validation.isBlank(note))
        {
            str += " (" + note + ")";
        }

        return str;
    }


    public static List<TransactionSpecific> consolidate(final List<TransactionSpecific> suggestions)
    {
        Map<String,TransactionSpecific> byKey = new HashMap<>();

        for(TransactionSpecific s : suggestions)
        {
            String key = s.where().getId() + ":" + s.whatAsset();
            if(byKey.containsKey(key))
            {
                // merge and replace
                byKey.put(key, TransactionSpecific.consolidate(s, byKey.get(key)));
            }
            else
            {
                byKey.put(key, s);
            }
        }
        return new ArrayList<>(byKey.values());
    }

    public static TransactionSpecific consolidate(final TransactionSpecific t1, final TransactionSpecific t2)
    {
        // assume that for ones matching acctId+whatAsset, that most items can be taken from t1
        return new TransactionSpecific(
                t1.assetClass,
                t1.whatAsset, // either a ticker, or a asset name
                sum(t1.howMuchUnits, t2.howMuchUnits),
                sum(t1.howMuchDollars, t2.howMuchDollars),
                t1.where,
                coalesce(t1.note, t2.note)
                );
    }

    private static <T> T coalesce(final T a, final T b)
    {
        return a == null ? b : a;
    }


    private static Double sum(final Double amt1, final Double amt2)
    {
        if(amt1 == null)
        {
            return amt2;
        }
        if(amt2 == null)
        {
            return amt1;
        }
        return amt1 + amt2;
    }
}
