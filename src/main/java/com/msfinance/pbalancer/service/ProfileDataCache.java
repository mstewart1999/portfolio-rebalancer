package com.msfinance.pbalancer.service;

import java.io.IOException;

public class ProfileDataCache
{
    private static ProfileData instance;

    public static ProfileData get()
    {
        return instance;
    }

    public static synchronized void switchProfile(final String profileId) throws IOException
    {
        instance = new ProfileData(profileId);
    }
}
