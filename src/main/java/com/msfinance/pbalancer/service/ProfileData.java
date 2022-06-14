package com.msfinance.pbalancer.service;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.msfinance.pbalancer.StateManager;
import com.msfinance.pbalancer.model.Account;
import com.msfinance.pbalancer.model.Asset;
import com.msfinance.pbalancer.model.InvalidDataException;
import com.msfinance.pbalancer.model.Portfolio;
import com.msfinance.pbalancer.model.Profile;
import com.msfinance.pbalancer.model.ProfileSettings;
import com.msfinance.pbalancer.model.aa.AssetClass;

public class ProfileData
{
    private static final Logger LOG = LoggerFactory.getLogger(ProfileData.class);

    private final Profile profile;
    private final List<ProfileSettings> settings;
    private final Map<String,Portfolio> portfolios = new HashMap<>();
    private final Map<String,Account> accounts = new HashMap<>();
    private final Map<String,Asset> assets = new HashMap<>();

    public ProfileData(final String id) throws IOException
    {
        profile = DataFactory.get().getProfile(id);
        settings = DataFactory.get().listSettingsForProfile(id);

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

        if(settings.size() > 0)
        {
            profile.setSettings(settings.get(0));
            if(settings.size() > 1)
            {
                LOG.warn("Excess profile settings: {}", settings.size());
            }
        }
        else
        {
            // create some defaults
            profile.setSettings(new ProfileSettings(id));
            DataFactory.get().createSettings(profile.getSettings());
        }

        // map each parent/child;
        for(Asset a : assets.values())
        {
            Account parent = accounts.get(a.getAccountId());
            if(parent == null)
            {
                LOG.warn("Orphaned asset: {}", a.getId());
            }
            else
            {
                parent.getAssets().add(a);
                a.setAccount(parent);
            }
            // check for alerts
            a.validate();

            // deal with custom asset classes
            AssetClass.add(a.getAssetClass());
        }
        for(Account a : accounts.values())
        {
            Portfolio parent = portfolios.get(a.getPortfolioId());
            if(parent == null)
            {
                LOG.warn("Orphaned account: {}", a.getId());
            }
            else
            {
                parent.getAccounts().add(a);
                a.setPortfolio(parent);
            }
        }
        for(Portfolio p : portfolios.values())
        {
            Profile parent = profile;
            if(parent == null)
            {
                LOG.warn("Orphaned portfolio: {}", p.getId());
            }
            else
            {
                parent.getPortfolios().add(p);
                p.setProfile(parent);
            }
            if(p.getTargetAA() != null)
            {
                // check for alerts
                try
                {
                    p.getTargetAA().getRoot().validate();
                }
                catch (InvalidDataException e)
                {
                    // ignore here
                }

                // deal with custom asset classes
                p.getTargetAA().getRoot().allLeaves().stream()
                    .forEach(n -> AssetClass.add(n.getName()));
            }
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
