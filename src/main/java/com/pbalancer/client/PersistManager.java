package com.pbalancer.client;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pbalancer.client.model.Account;
import com.pbalancer.client.model.Asset;
import com.pbalancer.client.model.Portfolio;
import com.pbalancer.client.model.Profile;
import com.pbalancer.client.model.aa.PreferredAsset;
import com.pbalancer.client.service.DataFactory;

public class PersistManager
{
    private static final Logger LOG = LoggerFactory.getLogger(PersistManager.class);

    /**
     * Save all dirty objects
     * @param profile
     * @throws IOException
     */
    public static boolean persistAll(final Profile profile) throws IOException
    {
        int numProfileUpdates = 0;
        int numSettingsUpdates = 0;
        int numPortfolioUpdates = 0;
        int numAccountUpdates = 0;
        int numAssetUpdates = 0;
        int numAssetClassMappingUpdates = 0;

        // TODO: not sure if it would be worth it to structure rest API with bulk updates, or just traditional single record updates
        // TODO: implement retries?
        if(profile != null)
        {
            if(profile.isDirty())
            {
                DataFactory.get().updateProfile(profile);
                profile.markClean();
                numProfileUpdates++;
            }

            if((profile.getSettings() != null) && profile.getSettings().isDirty())
            {
                DataFactory.get().updateSettings(profile.getSettings());
                profile.getSettings().markClean();
                numSettingsUpdates++;
            }

            for(Portfolio p : profile.getPortfolios())
            {
                if(p.isDirty())
                {
                    DataFactory.get().updatePortfolio(p);
                    p.markClean();
                    numPortfolioUpdates++;
                }

                for(Account ac : p.getAccounts())
                {
                    if(ac.isDirty())
                    {
                        DataFactory.get().updateAccount(ac);
                        ac.markClean();
                        numAccountUpdates++;
                    }

                    for(Asset as : ac.getAssets())
                    {
                        if(as.isDirty())
                        {
                            DataFactory.get().updateAsset(as);
                            as.markClean();
                            numAssetUpdates++;
                        }
                    }
                }

                if(p.getAssetClassMappings() != null)
                {
                    for(PreferredAsset acm : p.getAssetClassMappings())
                    {
                        if(acm.isDirty())
                        {
                            DataFactory.get().updateAssetClassMapping(acm);
                            acm.markClean();
                            numAssetClassMappingUpdates++;
                        }
                    }
                }
            }
        }

        LOG.debug(
                "Persisted: {} profiles, {} settings, {} portfolios, {} accounts, {} assets, {} acm",
                numProfileUpdates, numSettingsUpdates, numPortfolioUpdates, numAccountUpdates, numAssetUpdates, numAssetClassMappingUpdates);
        return true;
    }
}
