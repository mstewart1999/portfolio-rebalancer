package com.msfinance.pbalancer.model.rebalance;

import com.msfinance.pbalancer.model.Portfolio;

public record PortfolioCashSuggestionsRequest(Portfolio portfolio, TempCash cash)
{
}
