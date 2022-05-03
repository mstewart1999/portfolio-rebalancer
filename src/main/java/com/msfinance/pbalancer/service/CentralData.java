package com.msfinance.pbalancer.service;

import java.io.IOException;
import java.util.List;

import com.msfinance.pbalancer.model.Account;
import com.msfinance.pbalancer.model.Asset;
import com.msfinance.pbalancer.model.Portfolio;
import com.msfinance.pbalancer.model.Profile;

public class CentralData implements IData
{

    @Override
    public Profile getProfile(final String profileId) throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void createProfile(final Profile p) throws IOException
    {
        // TODO implement server and REST call
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateProfile(final Profile p) throws IOException
    {
        // TODO implement server and REST call
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Portfolio> listPortfoliosForProfile(final String profileId) throws IOException
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


    @Override
    public List<Account> listAccountsForProfile(final String profileId) throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Account> listAccountsForPortfolio(final String profileId, final String portfolioId) throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Account getAccount(final String profileId, final String id) throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void createAccount(final Account a) throws IOException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateAccount(final Account a) throws IOException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteAccount(final Account a) throws IOException
    {
        // TODO Auto-generated method stub

    }


    @Override
    public List<Asset> listAssetsForProfile(final String profileId) throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Asset> listAssetsForPortfolio(final String profileId, final String portfolioId) throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Asset getAsset(final String profileId, final String id) throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void createAsset(final Asset a) throws IOException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateAsset(final Asset a) throws IOException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteAsset(final Asset a) throws IOException
    {
        // TODO Auto-generated method stub

    }

}
