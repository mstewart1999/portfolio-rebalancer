package com.msfinance.pbalancer.model.rebalance;

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
}
