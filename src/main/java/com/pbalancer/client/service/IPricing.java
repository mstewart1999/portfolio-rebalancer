package com.pbalancer.client.service;

import java.util.Collection;
import java.util.Map;

import com.pbalancer.client.model.PriceResult;

public interface IPricing
{
    Map<String,PriceResult> getMostRecentEOD(Collection<String> tickers) throws ServiceException;
}
