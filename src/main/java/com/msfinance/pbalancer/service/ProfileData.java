package com.msfinance.pbalancer.service;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.msfinance.pbalancer.StateManager;
import com.msfinance.pbalancer.model.Account;
import com.msfinance.pbalancer.model.Asset;
import com.msfinance.pbalancer.model.Portfolio;
import com.msfinance.pbalancer.model.Profile;

public class ProfileData
{
    private final Profile profile;
    private final Map<String,Portfolio> portfolios = new HashMap<>();
    private final Map<String,Account> accounts = new HashMap<>();
    private final Map<String,Asset> assets = new HashMap<>();

    public ProfileData(final String id) throws IOException
    {
        profile = DataFactory.get().getProfile(id);

        DataFactory.get().listPortfoliosForProfile(id)
            .stream()
            .forEach(p -> portfolios.put(p.getId(), p));

        DataFactory.get().listAccountsForProfile(id)
            .stream()
            .forEach(a -> accounts.put(a.getId(), a));

        DataFactory.get().listAssetsForProfile(id)
            .stream()
            .forEach(a -> assets.put(a.getId(), a))
            ;

        // map each parent/child;
        for(Asset a : assets.values())
        {
            Account parent = accounts.get(a.getAccountId());
            parent.getAssets().add(a);
            a.setAccount(parent);
        }
        for(Account a : accounts.values())
        {
            Portfolio parent = portfolios.get(a.getPortfolioId());
            parent.getAccounts().add(a);
            a.setPortfolio(parent);
        }
        for(Portfolio p : portfolios.values())
        {
            Profile parent = profile;
            parent.getPortfolios().add(p);
            p.setProfile(parent);
        }

        sortRecursive(profile);
        StateManager.recalculateRecursive(profile);
    }

    private static void sortRecursive(final Profile profile)
    {
        for(Portfolio p : profile.getPortfolios())
        {
            for(Account ac : p.getAccounts())
            {
                for(Asset as : ac.getAssets())
                {
                    // no-op
                }
                ac.getAssets().sort(Comparator.comparing(Asset::getListPosition));
            }
            p.getAccounts().sort(Comparator.comparing(Account::getListPosition));
        }
        profile.getPortfolios().sort(Comparator.comparing(Portfolio::getListPosition));
    }


    public Profile getProfile()
    {
        return profile;
    }
}
