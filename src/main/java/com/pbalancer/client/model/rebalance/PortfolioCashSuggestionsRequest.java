package com.pbalancer.client.model.rebalance;

import com.pbalancer.client.model.Portfolio;

public record PortfolioCashSuggestionsRequest(Portfolio portfolio, TempCash cash)
{
}
