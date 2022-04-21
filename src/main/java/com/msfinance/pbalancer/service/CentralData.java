package com.msfinance.pbalancer.service;

import java.io.IOException;
import java.util.List;

import com.msfinance.pbalancer.model.Portfolio;

public class CentralData implements IData
{

    @Override
    public List<Portfolio> getPortfolios(final String profileId) throws IOException
    {
        // TODO implement server and REST call
        throw new UnsupportedOperationException();
    }

    @Override
    public Portfolio getPortfolio(final String profileId, final String id) throws IOException
    {
        // TODO implement server and REST call
        throw new UnsupportedOperationException();
    }

    @Override
    public void createPortfolio(final Portfolio p) throws IOException
    {
        // TODO implement server and REST call
        throw new UnsupportedOperationException();
    }

    @Override
    public void updatePortfolio(final Portfolio p) throws IOException
    {
        // TODO implement server and REST call
        throw new UnsupportedOperationException();
    }

    @Override
    public void deletePortfolio(final Portfolio p) throws IOException
    {
        // TODO implement server and REST call
        throw new UnsupportedOperationException();
    }

}
