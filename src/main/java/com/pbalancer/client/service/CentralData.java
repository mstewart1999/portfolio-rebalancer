package com.pbalancer.client.service;

import java.io.IOException;
import java.util.List;

import com.pbalancer.client.model.Account;
import com.pbalancer.client.model.Asset;
import com.pbalancer.client.model.Portfolio;
import com.pbalancer.client.model.Profile;
import com.pbalancer.client.model.ProfileSettings;
import com.pbalancer.client.model.aa.PreferredAsset;

public class CentralData implements IData
{

    @Override
    public List<Profile> listProfiles() throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

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
    public void deleteProfile(final Profile p) throws IOException
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public List<ProfileSettings> listSettingsForProfile(final String profileId) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProfileSettings getSettings(final String profileId, final String id) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createSettings(final ProfileSettings s) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateSettings(final ProfileSettings s) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteSettings(final ProfileSettings s) throws IOException
    {
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

    @Override
    public List<PreferredAsset> listAssetClassMappingsForProfile(final String profileId) throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PreferredAsset getAssetClassMapping(final String profileId, final String id) throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void createAssetClassMapping(final PreferredAsset acm) throws IOException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateAssetClassMapping(final PreferredAsset acm) throws IOException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteAssetClassMapping(final PreferredAsset acm) throws IOException
    {
        // TODO Auto-generated method stub

    }

}
