package com.pbalancer.client.service;

import com.pbalancer.client.Config;

public class DataFactory
{
    private enum Impl { LOCAL, CENTRAL }

    public static IData get()
    {
        Impl impl = Impl.valueOf(Config.getInstance().getDataImpl());
        switch(impl)
        {
        case LOCAL: return getLocal();
        case CENTRAL: return new CentralData();
        default: throw new IllegalArgumentException("Unhandled enum " + impl);
        }
    }

    private static IData getLocal()
    {
        // any differences for android or ios?
        // if(com.gluonhq.attach.util.Platform.isDesktop())

        if(org.controlsfx.tools.Platform.getCurrent() == org.controlsfx.tools.Platform.WINDOWS)
        {
            return new WinLocalData();
        }
        throw new UnsupportedOperationException("Only windows :(");
    }
}
