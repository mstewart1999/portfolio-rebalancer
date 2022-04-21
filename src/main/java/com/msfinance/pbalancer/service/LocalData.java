package com.msfinance.pbalancer.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.msfinance.pbalancer.model.Portfolio;
import com.msfinance.pbalancer.util.JSONHelper;

public abstract class LocalData implements IData
{

    protected abstract Path getDataDir();

    @Override
    public List<Portfolio> getPortfolios(final String profileId) throws IOException
    {
        Path dataDir = getDataDir().resolve(profileId).resolve("portfolio");
        Files.createDirectories(dataDir);

        List<String> ids = Files
            .list(dataDir)
            .filter(p -> Files.isRegularFile(p))
            .filter(p -> p.getFileName().toString().endsWith(".data"))
            .map(p -> p.getFileName().toString().replace(".data", ""))
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
        Path path = getDataDir().resolve(profileId).resolve("portfolio").resolve(id + ".data");
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
        Path path = getDataDir().resolve(p.getProfileId()).resolve("portfolio").resolve(p.getId() + ".data");
        Files.createDirectories(path.getParent());

        String str = JSONHelper.toJson(p);
        Files.writeString(path, str);
    }

    @Override
    public void updatePortfolio(final Portfolio p) throws IOException
    {
        Path path = getDataDir().resolve(p.getProfileId()).resolve("portfolio").resolve(p.getId() + ".data");
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
        Path path = getDataDir().resolve(p.getProfileId()).resolve("portfolio").resolve(p.getId() + ".data");
        Files.deleteIfExists(path);
        // TODO: remove linked AA, Accounts, Assets, Alerts, etc...
        // old code when structure was different
//        Files.walkFileTree(
//                path,
//                new SimpleFileVisitor<>() {
//
//                    // delete directories or folders
//                    @Override
//                    public FileVisitResult postVisitDirectory(final Path dir,
//                            final IOException exc)
//                                    throws IOException {
//                        Files.delete(dir);
//                        return FileVisitResult.CONTINUE;
//                    }
//
//                    // delete files
//                    @Override
//                    public FileVisitResult visitFile(final Path file,
//                            final BasicFileAttributes attrs)
//                                    throws IOException {
//                        Files.delete(file);
//                        return FileVisitResult.CONTINUE;
//                    }
//                }
//            );
    }

}
