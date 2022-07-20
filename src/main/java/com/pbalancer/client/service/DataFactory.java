package com.pbalancer.client.service;

import com.gluonhq.attach.util.Platform;

public class DataFactory
{
    public static IData get()
    {
        if(Platform.isDesktop()) // TODO: preference
        {
            if(org.controlsfx.tools.Platform.getCurrent() == org.controlsfx.tools.Platform.WINDOWS)
            {
                return new WinLocalData();
            }
            throw new UnsupportedOperationException("Only windows :(");
        }
        return new CentralData();
    }
}
