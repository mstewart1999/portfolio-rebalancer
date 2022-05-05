package com.msfinance.pbalancer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.msfinance.pbalancer.model.Account;
import com.msfinance.pbalancer.model.Asset;
import com.msfinance.pbalancer.model.Portfolio;
import com.msfinance.pbalancer.model.Profile;
import com.msfinance.pbalancer.service.DataFactory;

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
        int numPortfolioUpdates = 0;
        int numAccountUpdates = 0;
        int numAssetUpdates = 0;
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
            }
        }
        LOG.debug("Persisted: {} profiles, {} portfolios, {} accounts, {} assets", numProfileUpdates, numPortfolioUpdates, numAccountUpdates, numAssetUpdates);
        return true;
    }
}
