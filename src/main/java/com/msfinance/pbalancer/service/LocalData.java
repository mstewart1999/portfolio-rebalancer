package com.msfinance.pbalancer.service;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Collectors;

import com.msfinance.pbalancer.model.Portfolio;
import com.msfinance.pbalancer.util.JSONHelper;

public abstract class LocalData implements IData
{

    protected abstract Path getDataDir();

    @Override
    public List<String> getPortfolioIds() throws IOException
    {
        Path dataDir = getDataDir();
        Files.createDirectories(dataDir);

        return Files
            .list(dataDir)
            .filter(p -> Files.isDirectory(p))
            .map(p -> p.getFileName().toString())
            .sorted()
            .collect(Collectors.toList());
    }

    @Override
    public Portfolio getPortfolio(final String id) throws IOException
    {
        Path path = getDataDir().resolve(id);
        if(!Files.isDirectory(path))
        {
            throw new IOException("Portfolio has no directory " + path);
        }

        String str = Files.readString(path.resolve("portfolio.json"));
        return JSONHelper.fromJson(str, Portfolio.class);
    }

    @Override
    public void createPortfolio(final Portfolio p) throws IOException
    {
        Path path = getDataDir().resolve(p.getId());
        Files.createDirectories(path);

        String str = JSONHelper.toJson(p);
        Files.writeString(path.resolve("portfolio.json"), str);
    }

    @Override
    public void updatePortfolio(final Portfolio p) throws IOException
    {
        Path path = getDataDir().resolve(p.getId());
        if(!Files.isDirectory(path))
        {
            throw new IOException("Portfolio has no directory " + path);
        }

        String str = JSONHelper.toJson(p);
        Files.writeString(path.resolve("portfolio.json"), str);
    }

    @Override
    public void deletePortfolio(final String id) throws IOException
    {
        Path path = getDataDir().resolve(id);
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
