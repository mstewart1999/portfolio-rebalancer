package com.pbalancer.client.service;

import java.io.IOException;
import java.util.List;

import com.pbalancer.client.model.Account;
import com.pbalancer.client.model.Asset;
import com.pbalancer.client.model.Portfolio;
import com.pbalancer.client.model.Profile;
import com.pbalancer.client.model.ProfileSettings;
import com.pbalancer.client.model.aa.PreferredAsset;

public interface IData
{
    List<Profile> listProfiles() throws IOException;
    Profile getProfile(String profileId) throws IOException;
    void createProfile(Profile p) throws IOException;
    void updateProfile(Profile p) throws IOException;
    void deleteProfile(Profile p) throws IOException;

    List<ProfileSettings> listSettingsForProfile(String profileId) throws IOException;
    ProfileSettings getSettings(String profileId, String id) throws IOException;
    void createSettings(ProfileSettings s) throws IOException;
    void updateSettings(ProfileSettings s) throws IOException;
    void deleteSettings(ProfileSettings s) throws IOException;

    List<Portfolio> listPortfoliosForProfile(String profileId) throws IOException;
    Portfolio getPortfolio(String profileId, String id) throws IOException;
    void createPortfolio(Portfolio p) throws IOException;
    void updatePortfolio(Portfolio p) throws IOException;
    void deletePortfolio(Portfolio p) throws IOException;

    List<Account> listAccountsForProfile(String profileId) throws IOException;
    Account getAccount(String profileId, String id) throws IOException;
    void createAccount(Account a) throws IOException;
    void updateAccount(Account a) throws IOException;
    void deleteAccount(Account a) throws IOException;

    List<Asset> listAssetsForProfile(String profileId) throws IOException;
    Asset getAsset(String profileId, String id) throws IOException;
    void createAsset(Asset a) throws IOException;
    void updateAsset(Asset a) throws IOException;
    void deleteAsset(Asset a) throws IOException;

    List<PreferredAsset> listAssetClassMappingsForProfile(String profileId) throws IOException;
    PreferredAsset getAssetClassMapping(String profileId, String id) throws IOException;
    void createAssetClassMapping(PreferredAsset acm) throws IOException;
    void updateAssetClassMapping(PreferredAsset acm) throws IOException;
    void deleteAssetClassMapping(PreferredAsset acm) throws IOException;
}
