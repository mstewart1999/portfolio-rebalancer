package com.msfinance.pbalancer;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import com.msfinance.pbalancer.model.Account;
import com.msfinance.pbalancer.model.Asset;
import com.msfinance.pbalancer.model.Portfolio;
import com.msfinance.pbalancer.model.Profile;
import com.msfinance.pbalancer.model.aa.AssetAllocation;
import com.msfinance.pbalancer.util.NumberFormatHelper;

public class StateManager
{
    public static Profile currentProfile;
    public static Portfolio currentPortfolio;
    public static Account currentAccount;
    public static Asset currentAsset;
    public static AssetAllocation currentAssetAllocation;
    public static String currentUrl;

    public static void reset()
    {
        currentProfile = null;
        currentPortfolio = null;
        currentAccount = null;
        currentAsset = null;
        currentAssetAllocation = null;
        currentUrl = null;
    }

    public static void recalculateProfileValue()
    {
        if(currentProfile != null)
        {
            Optional<BigDecimal> sum = currentProfile.getPortfolios()
                    .stream()
                    .map(p -> (p.getLastValue() == null) ? BigDecimal.ZERO : p.getLastValue())
                    .reduce((v1,v2) -> NumberFormatHelper.sum(v1,v2));
            if(sum.isPresent())
            {
                currentProfile.setLastValue(sum.get());
                currentProfile.setLastValueTmstp(new Date());
            }
            else
            {
                currentProfile.setLastValue(null);
                currentProfile.setLastValueTmstp(null);
            }
        }
    }

    public static void recalculatePortfolioValue()
    {
        if(currentPortfolio != null)
        {
            Optional<BigDecimal> sum = currentPortfolio.getAccounts()
                    .stream()
                    .map(a -> (a.getLastValue() == null) ? BigDecimal.ZERO : a.getLastValue())
                    .reduce((v1,v2) -> NumberFormatHelper.sum(v1,v2));
            if(sum.isPresent())
            {
                currentPortfolio.setLastValue(sum.get());
                currentPortfolio.setLastValueTmstp(new Date());
            }
            else
            {
                currentPortfolio.setLastValue(null);
                currentPortfolio.setLastValueTmstp(null);
            }
        }
    }

    public static void recalculateAccountValue()
    {
        if(currentAccount != null)
        {
            Optional<BigDecimal> sum = currentAccount.getAssets()
                    .stream()
                    .map(a -> (a.getBestTotalValue() == null) ? BigDecimal.ZERO : a.getBestTotalValue())
                    .reduce((v1,v2) -> NumberFormatHelper.sum(v1,v2));
            if(sum.isPresent())
            {
                currentAccount.setLastValue(sum.get());
                currentAccount.setLastValueTmstp(new Date());
            }
            else
            {
                currentAccount.setLastValue(null);
                currentAccount.setLastValueTmstp(null);
            }
        }
    }
}
