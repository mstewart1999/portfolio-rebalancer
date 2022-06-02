package com.msfinance.pbalancer.model.rebalance;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.msfinance.pbalancer.model.Account;

/**
 * Utility class for use in rebalancing operations.
 * Keeps track of positive/negative cash (by account) that has been freed
 * or is needed to perform transactions.
 */
public class TempCash
{
    private static final double CENT = 0.01;

    private final Map<Account,Double> cashByAcct = new LinkedHashMap<>();


    public void add(final Account acct, final double amt)
    {
        if(cashByAcct.containsKey(acct))
        {
            cashByAcct.put(acct, cashByAcct.get(acct) + amt);
        }
        else
        {
            cashByAcct.put(acct, amt);
        }
    }

    public void subtract(final Account acct, final double amt)
    {
        if(cashByAcct.containsKey(acct))
        {
            cashByAcct.put(acct, cashByAcct.get(acct) - amt);
        }
        else
        {
            cashByAcct.put(acct, -amt);
        }
    }

    public double total()
    {
        return cashByAcct.values().stream().reduce((a,b) -> (a == null) ? b : a+b).orElse(0.0);
    }

    public Collection<Account> getAccounts()
    {
        return cashByAcct.keySet();
    }

    public double getCash(final Account acct)
    {
        return cashByAcct.getOrDefault(acct, 0.0);
    }

    public boolean hasCash(final Account acct)
    {
        if(cashByAcct.containsKey(acct))
        {
            double cash = getCash(acct);
            if((cash >= CENT) || (cash <= -CENT))
            {
                // positive or negative, but not zero
                return true;
            }
        }
        return false;
    }

    public static TempCash sum(final TempCash tc1, final TempCash tc2)
    {
        TempCash merged = new TempCash();

        for(Account a : tc1.getAccounts())
        {
            merged.add(a, tc1.getCash(a));
        }
        for(Account a : tc2.getAccounts())
        {
            merged.add(a, tc2.getCash(a));
        }

        return merged;
    }

    public TempCash positiveOnly()
    {
        TempCash out = new TempCash();
        for(Account a : getAccounts())
        {
            double cash = getCash(a);
            if(cash >= CENT)
            {
                out.add(a, cash);
            }
        }
        return out;
    }

    public TempCash negativeOnly()
    {
        TempCash out = new TempCash();
        for(Account a : getAccounts())
        {
            double cash = getCash(a);
            if(cash <= -CENT)
            {
                // adding a negative retains sign
                out.add(a, cash);
            }
        }
        return out;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for(Account a : getAccounts())
        {
            sb.append("'");
            sb.append(a.getName());
            sb.append("'=");
            sb.append(getCash(a));
            sb.append(", ");
        }
        return sb.toString();
    }
}
