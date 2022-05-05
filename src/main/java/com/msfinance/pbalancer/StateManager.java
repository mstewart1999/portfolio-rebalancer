package com.msfinance.pbalancer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
            Optional<BigDecimal> sum = profile.getPortfolios()
                    .stream()
                    .map(p -> (p.getLastValue() == null) ? BigDecimal.ZERO : p.getLastValue())
                    .reduce((v1,v2) -> NumberFormatHelper.sum(v1,v2));
            if(sum.isPresent())
            {
                if(!sum.get().equals(profile.getLastValue()))
                {
                    LOG.debug("Profile value changed by recalculation: old={} new={}", profile.getLastValue(), sum.get());
                    profile.setLastValue(sum.get());
                    profile.setLastValueTmstp(new Date()); // TODO
                    profile.markDirty();
                }
            }
            else
            {
                profile.setLastValue(null);
                profile.setLastValueTmstp(null);
                profile.markDirty();
            }
        }
    }

    public static void recalculatePortfolioValue(final Portfolio portfolio)
    {
        if(portfolio != null)
        {
            Optional<BigDecimal> sum = portfolio.getAccounts()
                    .stream()
                    .map(a -> (a.getLastValue() == null) ? BigDecimal.ZERO : a.getLastValue())
                    .reduce((v1,v2) -> NumberFormatHelper.sum(v1,v2));
            if(sum.isPresent())
            {
                if(!sum.get().equals(portfolio.getLastValue()))
                {
                    LOG.debug("Portfolio value changed by recalculation: old={} new={}", portfolio.getLastValue(), sum.get());
                    portfolio.setLastValue(sum.get());
                    portfolio.setLastValueTmstp(new Date()); // TODO
                    portfolio.markDirty();
                }
            }
            else
            {
                portfolio.setLastValue(null);
                portfolio.setLastValueTmstp(null);
                portfolio.markDirty();
            }
        }
    }

    public static void recalculateAccountValue(final Account account)
    {
        if(account != null)
        {
            Optional<BigDecimal> sum = account.getAssets()
                    .stream()
                    .map(a -> (a.getBestTotalValue() == null) ? BigDecimal.ZERO : a.getBestTotalValue())
                    .reduce((v1,v2) -> NumberFormatHelper.sum(v1,v2));
            if(sum.isPresent())
            {
                if(!sum.get().equals(account.getLastValue()))
                {
                    LOG.debug("Account value changed by recalculation: old={} new={}", account.getLastValue(), sum.get());
                    account.setLastValue(sum.get());
                    account.setLastValueTmstp(new Date()); // TODO
                    account.markDirty();
                }
            }
            else
            {
                account.setLastValue(null);
                account.setLastValueTmstp(null);
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
            PriceResult newPrice = priceUpdates.get(asset.getTicker());
            if(newPrice != null)
            {
                if(!Validation.isSame(asset.getLastAutoValue(), newPrice.price()) || !Validation.isSame(asset.getLastAutoValueTmstp(), newPrice.when()))
                {
                    asset.setLastAutoValue(newPrice.price());
                    asset.setLastAutoValueTmstp(newPrice.when());
                    asset.setManualValue(null);
                    asset.setManualValueTmstp(null);
                    asset.setPricingType(PricingType.AUTO_PER_UNIT);
                    asset.markDirty();
                    pendingSaveAsset.add(asset);
                    pendingSaveAccount.add(asset.getAccount());
                    pendingSavePortfolio.add(asset.getAccount().getPortfolio());
                    pendingSaveProfile.add(asset.getAccount().getPortfolio().getProfile());
                }
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
        Collection<String> tickers = Collections.singleton(asset.getTicker());
        Map<String,PriceResult> priceUpdates = PricingFactory.get().getMostRecentEOD(tickers);
        PriceResult newPrice = priceUpdates.get(asset.getTicker());
        if(newPrice != null)
        {
            if(!Validation.isSame(asset.getLastAutoValue(), newPrice.price()) || !Validation.isSame(asset.getLastAutoValueTmstp(), newPrice.when()))
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
        return false;
    }


    private static Set<String> uniqueTickers(final Collection<Asset> assets)
    {
        return assets.stream()
                .map(a -> a.getTicker())
                .filter(t -> !Validation.isBlank(t))
                .collect(Collectors.toSet());
    }

    private static Collection<Asset> listAssets(final Profile profile)
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

    private static Collection<Asset> listAssets(final Portfolio portfolio)
    {
        Collection<Asset> found = new ArrayList<>();

        for(Account acct : portfolio.getAccounts())
        {
            found.addAll(acct.getAssets());
        }
        return found;
    }

    private static Collection<Asset> listAssets(final Account account)
    {
        return account.getAssets();
    }
}
