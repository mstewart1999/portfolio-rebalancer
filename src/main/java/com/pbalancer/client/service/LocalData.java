package com.pbalancer.client.service;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.pbalancer.client.SystemSettings;
import com.pbalancer.client.model.Account;
import com.pbalancer.client.model.Asset;
import com.pbalancer.client.model.Portfolio;
import com.pbalancer.client.model.Profile;
import com.pbalancer.client.model.ProfileSettings;
import com.pbalancer.client.model.aa.PreferredAsset;
import com.pbalancer.client.util.JSONHelper;

public class LocalData implements IData
{
    private static final String PROFILE_DIR = "profile";
    private static final String SETTINGS_DIR = "settings";
    private static final String PORTFOLIO_DIR = "portfolio";
    private static final String ACCOUNT_DIR = "account";
    private static final String ASSET_DIR = "asset";
    private static final String ACM_DIR = "acm";

    private static final String SUFFIX = ".data";


    protected Path getDataDir()
    {
        return SystemSettings.getPBalancerDataDir().toPath();
    }



    @Override
    public List<Profile> listProfiles() throws IOException
    {
        Path dataDir = getDataDir();

        List<String> ids = Files
            .list(dataDir)
            .filter(p -> Files.isDirectory(p))
            .filter(p -> p.resolve(PROFILE_DIR).resolve(p.getFileName() + SUFFIX).toFile().isFile())
            .map(p -> p.getFileName().toString())
            .toList();
        List<Profile> items = new ArrayList<>();
        for(String id : ids)
        {
            items.add(getProfile(id));
        }
        return items;
    }

    @Override
    public Profile getProfile(final String profileId) throws IOException
    {
        Path path = getDataDir().resolve(profileId).resolve(PROFILE_DIR).resolve(profileId + SUFFIX);
        if(!Files.isRegularFile(path))
        {
            if(Profile.DEFAULT.equals(profileId))
            {
                // auto create this special local profile
                Profile p = new Profile(Profile.DEFAULT);
                // TODO:
                p.setName(Profile.DEFAULT);
                createProfile(p);
                return p;
            }
            else if(Profile.SAMPLE.equals(profileId))
            {
                // auto create this special local profile
                Profile p = new Profile(Profile.SAMPLE);
                // TODO:
                p.setName(Profile.SAMPLE);
                createProfile(p);
                return p;
            }
            else
            {
                throw new IOException("Portfolio not found " + path);
            }
        }

        String str = Files.readString(path);
        return JSONHelper.fromJson(str, Profile.class);
    }

    @Override
    public void createProfile(final Profile p) throws IOException
    {
        String profileId = p.getId();
        Path path = getDataDir().resolve(profileId).resolve(PROFILE_DIR).resolve(p.getId() + SUFFIX);
        Files.createDirectories(path.getParent());

        String str = JSONHelper.toJson(p);
        Files.writeString(path, str);
    }

    @Override
    public void updateProfile(final Profile p) throws IOException
    {
        String profileId = p.getId();
        Path path = getDataDir().resolve(profileId).resolve(PROFILE_DIR).resolve(p.getId() + SUFFIX);
        if(!Files.isRegularFile(path))
        {
            throw new IOException("Profile not found " + path);
        }

        String str = JSONHelper.toJson(p);
        Files.writeString(path, str);
    }

    @Override
    public void deleteProfile(final Profile p) throws IOException
    {
        String profileId = p.getId();
        Path pathToFile = getDataDir().resolve(profileId).resolve(PROFILE_DIR).resolve(p.getId() + SUFFIX);
        if(Files.exists(pathToFile))
        {
            Path path = getDataDir().resolve(profileId);
            Files.walkFileTree(
                    path,
                    new SimpleFileVisitor<>() {

                        // delete directories or folders
                        @Override
                        public FileVisitResult postVisitDirectory(final Path dir,
                                final IOException exc)
                                        throws IOException {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        }

                        // delete files
                        @Override
                        public FileVisitResult visitFile(final Path file,
                                final BasicFileAttributes attrs)
                                        throws IOException {
                            Files.delete(file);
                            return FileVisitResult.CONTINUE;
                        }
                    }
                );
        }
    }


    @Override
    public List<ProfileSettings> listSettingsForProfile(final String profileId) throws IOException
    {
        Path dataDir = getDataDir().resolve(profileId).resolve(SETTINGS_DIR);
        Files.createDirectories(dataDir);

        List<String> ids = Files
            .list(dataDir)
            .filter(p -> Files.isRegularFile(p))
            .filter(p -> p.getFileName().toString().endsWith(SUFFIX))
            .map(p -> p.getFileName().toString().replace(SUFFIX, ""))
            .collect(Collectors.toList());
        List<ProfileSettings> items = new ArrayList<>();
        for(String id : ids)
        {
            items.add(getSettings(profileId, id));
        }
        return items;
    }

    @Override
    public ProfileSettings getSettings(final String profileId, final String id) throws IOException
    {
        Path path = getDataDir().resolve(profileId).resolve(SETTINGS_DIR).resolve(id + SUFFIX);
        if(!Files.isRegularFile(path))
        {
            throw new IOException("ProfileSettings not found " + path);
        }

        String str = Files.readString(path);
        return JSONHelper.fromJson(str, ProfileSettings.class);
    }

    @Override
    public void createSettings(final ProfileSettings s) throws IOException
    {
        String profileId = s.getProfileId();
        Path path = getDataDir().resolve(profileId).resolve(SETTINGS_DIR).resolve(s.getId() + SUFFIX);
        Files.createDirectories(path.getParent());

        String str = JSONHelper.toJson(s);
        Files.writeString(path, str);
    }

    @Override
    public void updateSettings(final ProfileSettings s) throws IOException
    {
        String profileId = s.getProfileId();
        Path path = getDataDir().resolve(profileId).resolve(SETTINGS_DIR).resolve(s.getId() + SUFFIX);
        if(!Files.isRegularFile(path))
        {
            throw new IOException("ProfileSettings not found " + path);
        }

        String str = JSONHelper.toJson(s);
        Files.writeString(path, str);
    }

    @Override
    public void deleteSettings(final ProfileSettings s) throws IOException
    {
        String profileId = s.getProfileId();
        Path path = getDataDir().resolve(profileId).resolve(SETTINGS_DIR).resolve(s.getId() + SUFFIX);
        Files.deleteIfExists(path);
    }


    @Override
    public List<Portfolio> listPortfoliosForProfile(final String profileId) throws IOException
    {
        Path dataDir = getDataDir().resolve(profileId).resolve(PORTFOLIO_DIR);
        Files.createDirectories(dataDir);

        List<String> ids = Files
            .list(dataDir)
            .filter(p -> Files.isRegularFile(p))
            .filter(p -> p.getFileName().toString().endsWith(SUFFIX))
            .map(p -> p.getFileName().toString().replace(SUFFIX, ""))
            .collect(Collectors.toList());
        List<Portfolio> items = new ArrayList<>();
        for(String id : ids)
        {
            items.add(getPortfolio(profileId, id));
        }
        return items;
    }

    @Override
    public Portfolio getPortfolio(final String profileId, final String id) throws IOException
    {
        Path path = getDataDir().resolve(profileId).resolve(PORTFOLIO_DIR).resolve(id + SUFFIX);
        if(!Files.isRegularFile(path))
        {
            throw new IOException("Portfolio not found " + path);
        }

        String str = Files.readString(path);
        return JSONHelper.fromJson(str, Portfolio.class);
    }

    @Override
    public void createPortfolio(final Portfolio p) throws IOException
    {
        String profileId = p.getProfileId();
        Path path = getDataDir().resolve(profileId).resolve(PORTFOLIO_DIR).resolve(p.getId() + SUFFIX);
        Files.createDirectories(path.getParent());

        String str = JSONHelper.toJson(p);
        Files.writeString(path, str);
    }

    @Override
    public void updatePortfolio(final Portfolio p) throws IOException
    {
        String profileId = p.getProfileId();
        Path path = getDataDir().resolve(profileId).resolve(PORTFOLIO_DIR).resolve(p.getId() + SUFFIX);
        if(!Files.isRegularFile(path))
        {
            throw new IOException("Portfolio not found " + path);
        }

        String str = JSONHelper.toJson(p);
        Files.writeString(path, str);
    }

    @Override
    public void deletePortfolio(final Portfolio p) throws IOException
    {
        String profileId = p.getProfileId();
        Path path = getDataDir().resolve(profileId).resolve(PORTFOLIO_DIR).resolve(p.getId() + SUFFIX);
        Files.deleteIfExists(path);
        // TODO: remove linked AA, Accounts, Assets, Alerts, etc...
    }


    @Override
    public List<Account> listAccountsForProfile(final String profileId) throws IOException
    {
        Path dataDir = getDataDir().resolve(profileId).resolve(ACCOUNT_DIR);
        Files.createDirectories(dataDir);

        List<String> ids = Files
            .list(dataDir)
            .filter(p -> Files.isRegularFile(p))
            .filter(p -> p.getFileName().toString().endsWith(SUFFIX))
            .map(p -> p.getFileName().toString().replace(SUFFIX, ""))
            .collect(Collectors.toList());
        List<Account> items = new ArrayList<>();
        for(String id : ids)
        {
            items.add(getAccount(profileId, id));
        }
        return items;
    }

    @Override
    public Account getAccount(final String profileId, final String id) throws IOException
    {
        Path path = getDataDir().resolve(profileId).resolve(ACCOUNT_DIR).resolve(id + SUFFIX);
        if(!Files.isRegularFile(path))
        {
            throw new IOException("Account not found " + path);
        }

        String str = Files.readString(path);
        return JSONHelper.fromJson(str, Account.class);
    }

    @Override
    public void createAccount(final Account a) throws IOException
    {
        String profileId = a.getPortfolio().getProfileId();
        Path path = getDataDir().resolve(profileId).resolve(ACCOUNT_DIR).resolve(a.getId() + SUFFIX);
        Files.createDirectories(path.getParent());

        String str = JSONHelper.toJson(a);
        Files.writeString(path, str);
    }

    @Override
    public void updateAccount(final Account a) throws IOException
    {
        String profileId = a.getPortfolio().getProfileId();
        Path path = getDataDir().resolve(profileId).resolve(ACCOUNT_DIR).resolve(a.getId() + SUFFIX);
        if(!Files.isRegularFile(path))
        {
            throw new IOException("Account not found " + path);
        }

        String str = JSONHelper.toJson(a);
        Files.writeString(path, str);
    }

    @Override
    public void deleteAccount(final Account a) throws IOException
    {
        String profileId = a.getPortfolio().getProfileId();
        Path path = getDataDir().resolve(profileId).resolve(ACCOUNT_DIR).resolve(a.getId() + SUFFIX);
        Files.deleteIfExists(path);
    }


    @Override
    public List<Asset> listAssetsForProfile(final String profileId) throws IOException
    {
        Path dataDir = getDataDir().resolve(profileId).resolve(ASSET_DIR);
        Files.createDirectories(dataDir);

        List<String> ids = Files
            .list(dataDir)
            .filter(p -> Files.isRegularFile(p))
            .filter(p -> p.getFileName().toString().endsWith(SUFFIX))
            .map(p -> p.getFileName().toString().replace(SUFFIX, ""))
            .collect(Collectors.toList());
        List<Asset> items = new ArrayList<>();
        for(String id : ids)
        {
            items.add(getAsset(profileId, id));
        }
        return items;
    }

    @Override
    public Asset getAsset(final String profileId, final String id) throws IOException
    {
        Path path = getDataDir().resolve(profileId).resolve(ASSET_DIR).resolve(id + SUFFIX);
        if(!Files.isRegularFile(path))
        {
            throw new IOException("Asset not found " + path);
        }

        String str = Files.readString(path);
        return JSONHelper.fromJson(str, Asset.class);
    }

    @Override
    public void createAsset(final Asset a) throws IOException
    {
        String profileId = a.getAccount().getPortfolio().getProfileId();
        Path path = getDataDir().resolve(profileId).resolve(ASSET_DIR).resolve(a.getId() + SUFFIX);
        Files.createDirectories(path.getParent());

        String str = JSONHelper.toJson(a);
        Files.writeString(path, str);
    }

    @Override
    public void updateAsset(final Asset a) throws IOException
    {
        String profileId = a.getAccount().getPortfolio().getProfileId();
        Path path = getDataDir().resolve(profileId).resolve(ASSET_DIR).resolve(a.getId() + SUFFIX);
        if(!Files.isRegularFile(path))
        {
            throw new IOException("Asset not found " + path);
        }

        String str = JSONHelper.toJson(a);
        Files.writeString(path, str);
    }

    @Override
    public void deleteAsset(final Asset a) throws IOException
    {
        String profileId = a.getAccount().getPortfolio().getProfileId();
        Path path = getDataDir().resolve(profileId).resolve(ASSET_DIR).resolve(a.getId() + SUFFIX);
        Files.deleteIfExists(path);
    }


    @Override
    public List<PreferredAsset> listAssetClassMappingsForProfile(final String profileId) throws IOException
    {
        Path dataDir = getDataDir().resolve(profileId).resolve(ACM_DIR);
        Files.createDirectories(dataDir);

        List<String> ids = Files
            .list(dataDir)
            .filter(p -> Files.isRegularFile(p))
            .filter(p -> p.getFileName().toString().endsWith(SUFFIX))
            .map(p -> p.getFileName().toString().replace(SUFFIX, ""))
            .collect(Collectors.toList());
        List<PreferredAsset> items = new ArrayList<>();
        for(String id : ids)
        {
            items.add(getAssetClassMapping(profileId, id));
        }
        return items;
    }

    @Override
    public PreferredAsset getAssetClassMapping(final String profileId, final String id) throws IOException
    {
        Path path = getDataDir().resolve(profileId).resolve(ACM_DIR).resolve(id + SUFFIX);
        if(!Files.isRegularFile(path))
        {
            throw new IOException("AssetClassMapping not found " + path);
        }

        String str = Files.readString(path);
        return JSONHelper.fromJson(str, PreferredAsset.class);
    }

    @Override
    public void createAssetClassMapping(final PreferredAsset acm) throws IOException
    {
        String profileId = acm.getPortfolio().getProfileId();
        Path path = getDataDir().resolve(profileId).resolve(ACM_DIR).resolve(acm.getId() + SUFFIX);
        Files.createDirectories(path.getParent());

        String str = JSONHelper.toJson(acm);
        Files.writeString(path, str);
    }

    @Override
    public void updateAssetClassMapping(final PreferredAsset acm) throws IOException
    {
        String profileId = acm.getPortfolio().getProfileId();
        Path path = getDataDir().resolve(profileId).resolve(ACM_DIR).resolve(acm.getId() + SUFFIX);
        if(!Files.isRegularFile(path))
        {
            throw new IOException("AssetClassMapping not found " + path);
        }

        String str = JSONHelper.toJson(acm);
        Files.writeString(path, str);
    }

    @Override
    public void deleteAssetClassMapping(final PreferredAsset acm) throws IOException
    {
        String profileId = acm.getPortfolio().getProfileId();
        Path path = getDataDir().resolve(profileId).resolve(ACM_DIR).resolve(acm.getId() + SUFFIX);
        Files.deleteIfExists(path);
    }

}
