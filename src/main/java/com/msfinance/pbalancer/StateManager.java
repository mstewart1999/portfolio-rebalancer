package com.msfinance.pbalancer;

import com.msfinance.pbalancer.model.Portfolio;
import com.msfinance.pbalancer.model.aa.AssetAllocation;

public class StateManager
{
    public static String currentPortfolioId;
    public static Portfolio currentPortfolio;
    public static AssetAllocation currentAssetAllocation;
    public static String currentUrl;

    public static void reset()
    {
        currentPortfolioId = null;
        currentPortfolio = null;
        currentAssetAllocation = null;
        currentUrl = null;
    }
}
