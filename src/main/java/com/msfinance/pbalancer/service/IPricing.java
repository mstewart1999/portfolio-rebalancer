package com.msfinance.pbalancer.service;

import java.util.Collection;
import java.util.Map;

import com.msfinance.pbalancer.model.PriceResult;

public interface IPricing
{
    Map<String,PriceResult> getMostRecentEOD(Collection<String> tickers);
}