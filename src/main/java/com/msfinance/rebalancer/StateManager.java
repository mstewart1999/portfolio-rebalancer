package com.msfinance.rebalancer;

import com.msfinance.rebalancer.model.Portfolio;

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
