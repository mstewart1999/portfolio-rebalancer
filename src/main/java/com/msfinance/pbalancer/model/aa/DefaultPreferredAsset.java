package com.msfinance.pbalancer.model.aa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.msfinance.pbalancer.util.Validation;

public class DefaultPreferredAsset
{
    private static final Logger LOG = LoggerFactory.getLogger(DefaultPreferredAsset.class);

    private final String assetClass;
    private final String ticker;
    private final boolean isPrimary;

    public DefaultPreferredAsset(final String[] csvParts)
    {
        this(csvParts[0], csvParts[2], parseBoolean(csvParts[1]));
    }

    public DefaultPreferredAsset(final String assetClass, final String ticker, final boolean isPrimary)
    {
        this.assetClass = assetClass;
        this.ticker = ticker;
        this.isPrimary = isPrimary;
    }

    public String getAssetClass()
    {
        return assetClass;
    }

    public String getTicker()
    {
        return ticker;
    }

    public String getName()
    {
        return AssetTickerCache.getInstance().lookup(ticker).getName();
    }

    public boolean isPrimary()
    {
        return isPrimary;
    }

    public boolean isKeeper()
    {
        if(Validation.isBlank(assetClass))
        {
            LOG.error("DefaultPreferredAsset for blank asset class");
            return false;
        }
        if(Validation.isBlank(ticker))
        {
            LOG.error("DefaultPreferredAsset for blank ticker");
            return false;
        }

        if(AssetClass.lookup(assetClass) == null)
        {
            LOG.error("DefaultPreferredAsset for unknown asset class " + assetClass);
            //return false;
        }
        if(AssetTickerCache.getInstance().lookup(ticker) == null)
        {
            LOG.error("DefaultPreferredAsset for unknown ticker " + ticker);
            return false;
        }

        return true;
    }


    private static boolean parseBoolean(final String string)
    {
        return "Y".equals(string);
    }
}
