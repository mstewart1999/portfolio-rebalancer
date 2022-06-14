package com.msfinance.pbalancer.service;

import java.io.IOException;
import java.util.List;

import com.msfinance.pbalancer.model.Account;
import com.msfinance.pbalancer.model.Asset;
import com.msfinance.pbalancer.model.Portfolio;
import com.msfinance.pbalancer.model.Profile;
import com.msfinance.pbalancer.model.ProfileSettings;

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
    List<Account> listAccountsForPortfolio(String profileId, String portfolioId) throws IOException;
    Account getAccount(String profileId, String id) throws IOException;
    void createAccount(Account a) throws IOException;
    void updateAccount(Account a) throws IOException;
    void deleteAccount(Account a) throws IOException;

    List<Asset> listAssetsForProfile(String profileId) throws IOException;
    List<Asset> listAssetsForPortfolio(String profileId, String portfolioId) throws IOException;
    Asset getAsset(String profileId, String id) throws IOException;
    void createAsset(Asset a) throws IOException;
    void updateAsset(Asset a) throws IOException;
    void deleteAsset(Asset a) throws IOException;
}
