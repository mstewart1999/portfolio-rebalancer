package com.msfinance.pbalancer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.msfinance.pbalancer.model.Account;
import com.msfinance.pbalancer.model.Asset;
import com.msfinance.pbalancer.model.Asset.PricingType;
import com.msfinance.pbalancer.model.Portfolio;
import com.msfinance.pbalancer.model.PriceResult;
import com.msfinance.pbalancer.model.Profile;
import com.msfinance.pbalancer.service.PricingFactory;
import com.msfinance.pbalancer.util.NumberFormatHelper;
import com.msfinance.pbalancer.util.Validation;

public class StateManager
{
    private static final Logger LOG = LoggerFactory.getLogger(StateManager.class);

    /**
     * Do a fully bottom up recalculation of all totals.
     * Log any discrepancies, but do not persist here.
     * @param profile
     */
    public static void recalculateRecursive(final Profile profile)
    {
        if(profile != null)
        {
            for(Portfolio p : profile.getPortfolios())
            {
                for(Account ac : p.getAccounts())
                {
                    for(Asset as : ac.getAssets())
                    {
                        // no-op
                    }
                    recalculateAccountValue(ac);
                }
                recalculatePortfolioValue(p);
            }
            recalculateProfileValue(profile);
        }
    }


    public static void recalculateProfileValue(final Profile profile)
    {
        if(profile != null)
        {
            BigDecimal sum = profile.getPortfolios()
                    .stream()
                    .map(p -> (p.getLastValue() == null) ? BigDecimal.ZERO : p.getLastValue())
                    .reduce((v1,v2) -> NumberFormatHelper.sum(v1,v2))
                    .orElse(null);
            Date lastValueTmstpHigh = profile.getPortfolios()
                    .stream()
                    .map(a -> a.getLastValueTmstp())
                    .filter(d -> d != null)
                    .max(Comparator.naturalOrder())
                    .orElse(null);
            Date lastValueTmstpLow = profile.getPortfolios()
                    .stream()
                    .map(a -> a.getLastValueTmstpLow())
                    .filter(d -> d != null)
                    .min(Comparator.naturalOrder())
                    .orElse(null);
            if(sum != null)
            {
                if(!sum.equals(profile.getLastValue()))
                {
                    LOG.debug("Profile value changed by recalculation: old={} new={}", profile.getLastValue(), sum);
                    profile.setLastValue(sum);
                    profile.setLastValueTmstp(lastValueTmstpHigh);
                    profile.setLastValueTmstpLow(lastValueTmstpLow);
                    profile.markDirty();
                }
                else
                {
                    // leave no obvious trace
                    LOG.trace("Profile value not changed");
                }
            }
            else
            {
                profile.setLastValue(null);
                profile.setLastValueTmstp(null);
                profile.setLastValueTmstpLow(null);
                profile.markDirty();
            }
        }
    }

    public static void recalculatePortfolioValue(final Portfolio portfolio)
    {
        if(portfolio != null)
        {
            BigDecimal sum = portfolio.getAccounts()
                    .stream()
                    .map(a -> (a.getLastValue() == null) ? BigDecimal.ZERO : a.getLastValue())
                    .reduce((v1,v2) -> NumberFormatHelper.sum(v1,v2))
                    .orElse(null);
            Date lastValueTmstpHigh = portfolio.getAccounts()
                    .stream()
                    .map(a -> a.getLastValueTmstp())
                    .filter(d -> d != null)
                    .max(Comparator.naturalOrder())
                    .orElse(null);
            Date lastValueTmstpLow = portfolio.getAccounts()
                    .stream()
                    .map(a -> a.getLastValueTmstpLow())
                    .filter(d -> d != null)
                    .min(Comparator.naturalOrder())
                    .orElse(null);
            if(sum != null)
            {
                if(!sum.equals(portfolio.getLastValue()))
                {
                    LOG.debug("Portfolio value changed by recalculation: old={} new={}", portfolio.getLastValue(), sum);
                    portfolio.setLastValue(sum);
                    portfolio.setLastValueTmstp(lastValueTmstpHigh);
                    portfolio.setLastValueTmstpLow(lastValueTmstpLow);
                    portfolio.markDirty();
                }
                else
                {
                    // leave no obvious trace ;)
                    LOG.trace("Portfolio value not changed");
                }
            }
            else
            {
                portfolio.setLastValue(null);
                portfolio.setLastValueTmstp(null);
                portfolio.setLastValueTmstpLow(null);
                portfolio.markDirty();
            }
        }
    }

    public static void recalculateAccountValue(final Account account)
    {
        if(account != null)
        {
            BigDecimal sum = account.getAssets()
                    .stream()
                    .map(a -> (a.getBestTotalValue() == null) ? BigDecimal.ZERO : a.getBestTotalValue())
                    .reduce((v1,v2) -> NumberFormatHelper.sum(v1,v2))
                    .orElse(null);
            Date lastValueTmstpHigh = account.getAssets()
                    .stream()
                    .map(a -> a.getBestValueTmstp())
                    .filter(d -> d != null)
                    .max(Comparator.naturalOrder())
                    .orElse(null);
            Date lastValueTmstpLow = account.getAssets()
                    .stream()
                    .map(a -> a.getBestValueTmstp())
                    .filter(d -> d != null)
                    .min(Comparator.naturalOrder())
                    .orElse(null);
            if(sum != null)
            {
                if(!sum.equals(account.getLastValue()))
                {
                    LOG.debug("Account value changed by recalculation: old={} new={}", account.getLastValue(), sum);
                    account.setLastValue(sum);
                    account.setLastValueTmstp(lastValueTmstpHigh);
                    account.setLastValueTmstpLow(lastValueTmstpLow);
                    account.markDirty();
                }
                else
                {
                    // leave no obvious trace ;)
                    LOG.trace("Account value not changed");
                }
            }
            else
            {
                account.setLastValue(null);
                account.setLastValueTmstp(null);
                account.setLastValueTmstpLow(null);
                account.markDirty();
            }
        }
    }


    public static Collection<Asset> refreshPrices(final Profile profile)
    {
        Collection<Asset> assets = listAssets(profile);
        return refreshPrices(assets);
    }

    public static Collection<Asset> refreshPrices(final Portfolio portfolio)
    {
        Collection<Asset> assets = listAssets(portfolio);
        return refreshPrices(assets);
    }

    public static Collection<Asset> refreshPrices(final Account account)
    {
        Collection<Asset> assets = listAssets(account);
        return refreshPrices(assets);
    }

    private static Collection<Asset> refreshPrices(final Collection<Asset> assets)
    {
        Collection<String> tickers = uniqueTickers(assets);
        Map<String,PriceResult> priceUpdates = PricingFactory.get().getMostRecentEOD(tickers);

        List<Asset> pendingSaveAsset = new ArrayList<>();
        // TODO: .equals() and .hashCode()
        Set<Account> pendingSaveAccount = new HashSet<>();
        Set<Portfolio> pendingSavePortfolio = new HashSet<>();
        Set<Profile> pendingSaveProfile = new HashSet<>();
        for(Asset asset : assets)
        {
            if(applyPrice(asset, priceUpdates))
            {
                pendingSaveAsset.add(asset);
                pendingSaveAccount.add(asset.getAccount());
                pendingSavePortfolio.add(asset.getAccount().getPortfolio());
                pendingSaveProfile.add(asset.getAccount().getPortfolio().getProfile());
            }
        }

        // recalculate these changes bottom up
        for(Account account : pendingSaveAccount)
        {
            StateManager.recalculateAccountValue(account);
        }
        for(Portfolio portfolio : pendingSavePortfolio)
        {
            StateManager.recalculatePortfolioValue(portfolio);
        }
        for(Profile profile : pendingSaveProfile)
        {
            StateManager.recalculateProfileValue(profile);
        }
        return pendingSaveAsset;
    }


    public static boolean refreshPrice(final Asset asset)
    {
        Collection<String> tickers = new HashSet<>();
        if(hasRegularTicker(asset))
        {
            tickers.add(getRegularTicker(asset));
        }
        else if(hasProxyTicker(asset))
        {
            tickers.add(getProxyTicker(asset));
        }
        else
        {
            return false;
        }

        Map<String,PriceResult> priceUpdates = PricingFactory.get().getMostRecentEOD(tickers);

        return applyPrice(asset, priceUpdates);
    }

    private static boolean applyPrice(final Asset asset, final Map<String, PriceResult> priceUpdates)
    {
        if(hasRegularTicker(asset))
        {
            PriceResult newPrice = priceUpdates.get(getRegularTicker(asset));
            if(newPrice != null)
            {
                if(!Validation.isSame(
                        asset.getLastAutoValue(), newPrice.price())
                        || !Validation.isSame(asset.getLastAutoValueTmstp(), newPrice.when()))
                {
                    asset.setLastAutoValue(newPrice.price());
                    asset.setLastAutoValueTmstp(newPrice.when());
                    asset.setManualValue(null);
                    asset.setManualValueTmstp(null);
                    asset.setPricingType(PricingType.AUTO_PER_UNIT);
                    asset.markDirty();
                    return true;
                }
            }
        }
        else if(hasProxyTicker(asset))
        {
            PriceResult newPrice = priceUpdates.get(getProxyTicker(asset));
            if(newPrice != null)
            {
                if(!Validation.isSame(
                        asset.getProxy().getLastProxyValue(), newPrice.price())
                        || !Validation.isSame(asset.getProxy().getLastProxyValueTmstp(), newPrice.when()))
                {
                    asset.getProxy().setLastProxyValue(newPrice.price());
                    asset.getProxy().setLastProxyValueTmstp(newPrice.when());

                    BigDecimal calculatedValue = asset.getProxy().calculate();

                    asset.setLastAutoValue(calculatedValue);
                    asset.setLastAutoValueTmstp(newPrice.when());
                    asset.setManualValue(null);
                    asset.setManualValueTmstp(null);
                    asset.setPricingType(PricingType.AUTO_PER_UNIT);
                    asset.markDirty();
                    return true;
                }
            }
        }
        return false;
    }


    private static Set<String> uniqueTickers(final Collection<Asset> assets)
    {
        Set<String> tickers = new HashSet<>();

        for(Asset a : assets)
        {
            if(hasRegularTicker(a))
            {
                tickers.add(getRegularTicker(a));
            }
            else if(hasProxyTicker(a))
            {
                tickers.add(getProxyTicker(a));
            }
        }
        return tickers;
    }

    private static String getRegularTicker(final Asset a)
    {
        return a.getTicker();
    }
    private static String getProxyTicker(final Asset a)
    {
        if(a.getProxy() == null)
        {
            return null;
        }
        return a.getProxy().getProxyTicker();
    }

    private static boolean hasRegularTicker(final Asset a)
    {
        return !Validation.isBlank(a.getTicker());
    }
    private static boolean hasProxyTicker(final Asset a)
    {
        if(a.getProxy() == null)
        {
            return false;
        }
        return !Validation.isBlank(a.getProxy().getProxyTicker());
    }

    public static Collection<Asset> listAssets(final Profile profile)
    {
        Collection<Asset> found = new ArrayList<>();

        for(Portfolio portfolio : profile.getPortfolios())
        {
            for(Account acct : portfolio.getAccounts())
            {
                found.addAll(acct.getAssets());
            }
        }
        return found;
    }

    public static Collection<Asset> listAssets(final Portfolio portfolio)
    {
        Collection<Asset> found = new ArrayList<>();

        for(Account acct : portfolio.getAccounts())
        {
            found.addAll(acct.getAssets());
        }
        return found;
    }

    public static Collection<Asset> listAssets(final Account account)
    {
        return account.getAssets();
    }
}
