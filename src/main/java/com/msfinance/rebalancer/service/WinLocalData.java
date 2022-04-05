package com.msfinance.rebalancer.service;

import java.nio.file.Path;
import java.nio.file.Paths;

public class WinLocalData extends LocalData
{

    @Override
    protected Path getDataDir()
    {
        // TODO: cross platform!
        Path dataDir = Paths.get(System.getProperty("user.home"), "Documents", "msfinance-rebalancer");
        return dataDir;
    }

}
