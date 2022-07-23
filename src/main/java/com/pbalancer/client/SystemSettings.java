package com.pbalancer.client;

import java.io.File;

public class SystemSettings
{
    public static File getDocumentsDir()
    {
        // All of these solutions are bad
        //  - https://stackoverflow.com/questions/9677692/getting-my-documents-path-in-java
        //  - https://stackoverflow.com/questions/1503555/how-to-find-my-documents-folder-in-java
        // The "reg query" one is probably the best, but only for windows "user.home" exists on other OS
        // Lots of dumb ways to query registry:
        //  -  https://stackoverflow.com/questions/62289/read-write-to-windows-registry-using-java

        File homeDir = new File(System.getProperty("user.home"));

        File documentsDir = new File(homeDir, "Documents");
        if(documentsDir.exists())
        {
            return documentsDir;
        }

        // Windows XP and prior
        File mydocumentsDir = new File(homeDir, "My Documents");
        if(mydocumentsDir.exists())
        {
            return mydocumentsDir;
        }

        // getting desperate for a place
        if(homeDir.exists())
        {
            return homeDir;
        }

        // just giving up
        return new File(".");
    }

    public static File getPBalancerDataDir()
    {
        return new File(getDocumentsDir(), "pbalancer-data");
    }
}
