package com.msfinance.pbalancer;

import com.msfinance.pbalancer.model.Portfolio;

public class StateManager
{
    public static String currentPortfolioId;
    public static Portfolio currentPortfolio = null;

    public static void reset()
    {
        currentPortfolioId = null;
        currentPortfolio = null;
    }
}
