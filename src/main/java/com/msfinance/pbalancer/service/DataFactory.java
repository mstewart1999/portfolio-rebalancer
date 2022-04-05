package com.msfinance.pbalancer.service;

import com.gluonhq.attach.util.Platform;

public class DataFactory
{
    public static IData get()
    {
        if(Platform.isDesktop()) // TODO: preference
        {
            // https://stackoverflow.com/questions/14288185/detecting-windows-or-linux
            String osName = System.getProperty("os.name").toLowerCase(); // "windows 11"
            if(osName.contains("win"))
            {
                return new WinLocalData();
            }
            throw new UnsupportedOperationException("Only windows :(");
        }
        return new CentralData();
    }
}
