package com.msfinance.pbalancer;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.msfinance.pbalancer.model.Account;
import com.msfinance.pbalancer.model.Asset;
import com.msfinance.pbalancer.model.Portfolio;
import com.msfinance.pbalancer.model.Profile;
import com.msfinance.pbalancer.util.NumberFormatHelper;

public class StateManager
{
    private static final Logger LOG = LoggerFactory.getLogger(StateManager.class);

    /**
     * Do a fully bottom up recalculation of all totals.
     * Log any discrepancies, but do not persist here.
     * @param currentProfile
     */
    public static void recalculateRecursive(final Profile currentProfile)
    {
        if(currentProfile != null)
        {
            for(Portfolio p : currentProfile.getPortfolios())
            {
                for(Account ac : p.getAccounts())
                {
                    for(Asset as : ac.getAssets())
                    {
                        // no-op
                    }
                    recalculateAccountValue(ac, true);
                }
                recalculatePortfolioValue(p, true);
            }
            recalculateProfileValue(currentProfile, true);
        }
    }


    public static void recalculateProfileValue(final Profile currentProfile)
    {
        recalculateProfileValue(currentProfile, false);
    }

    public static void recalculateProfileValue(final Profile currentProfile, final boolean logChanges)
    {
        if(currentProfile != null)
        {
            Optional<BigDecimal> sum = currentProfile.getPortfolios()
                    .stream()
                    .map(p -> (p.getLastValue() == null) ? BigDecimal.ZERO : p.getLastValue())
                    .reduce((v1,v2) -> NumberFormatHelper.sum(v1,v2));
            if(sum.isPresent())
            {
                if(logChanges && !sum.get().equals(currentProfile.getLastValue()))
                {
                    LOG.debug("Profile value changed by recalculation: old={} new={}", currentProfile.getLastValue(), sum.get());
                }
                currentProfile.setLastValue(sum.get());
                currentProfile.setLastValueTmstp(new Date()); // TODO
            }
            else
            {
                currentProfile.setLastValue(null);
                currentProfile.setLastValueTmstp(null);
            }
        }
    }

    public static void recalculatePortfolioValue(final Portfolio currentPortfolio)
    {
        recalculatePortfolioValue(currentPortfolio, false);
    }

    public static void recalculatePortfolioValue(final Portfolio currentPortfolio, final boolean logChanges)
    {
        if(currentPortfolio != null)
        {
            Optional<BigDecimal> sum = currentPortfolio.getAccounts()
                    .stream()
                    .map(a -> (a.getLastValue() == null) ? BigDecimal.ZERO : a.getLastValue())
                    .reduce((v1,v2) -> NumberFormatHelper.sum(v1,v2));
            if(sum.isPresent())
            {
                if(logChanges && !sum.get().equals(currentPortfolio.getLastValue()))
                {
                    LOG.debug("Portfolio value changed by recalculation: old={} new={}", currentPortfolio.getLastValue(), sum.get());
                }
                currentPortfolio.setLastValue(sum.get());
                currentPortfolio.setLastValueTmstp(new Date()); // TODO
            }
            else
            {
                currentPortfolio.setLastValue(null);
                currentPortfolio.setLastValueTmstp(null);
            }
        }
    }

    public static void recalculateAccountValue(final Account currentAccount)
    {
        recalculateAccountValue(currentAccount, false);
    }

    public static void recalculateAccountValue(final Account currentAccount, final boolean logChanges)
    {
        if(currentAccount != null)
        {
            Optional<BigDecimal> sum = currentAccount.getAssets()
                    .stream()
                    .map(a -> (a.getBestTotalValue() == null) ? BigDecimal.ZERO : a.getBestTotalValue())
                    .reduce((v1,v2) -> NumberFormatHelper.sum(v1,v2));
            if(sum.isPresent())
            {
                if(logChanges && !sum.get().equals(currentAccount.getLastValue()))
                {
                    LOG.debug("Account value changed by recalculation: old={} new={}", currentAccount.getLastValue(), sum.get());
                }
                currentAccount.setLastValue(sum.get());
                currentAccount.setLastValueTmstp(new Date()); // TODO
            }
            else
            {
                currentAccount.setLastValue(null);
                currentAccount.setLastValueTmstp(null);
            }
        }
    }
}
