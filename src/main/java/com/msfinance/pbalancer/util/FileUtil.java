package com.msfinance.pbalancer.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtil
{
    public static boolean isNewerThanDays(final Path p, final int days)
    {
        if(!Files.exists(p))
        {
            return false;
        }
        long millis = days*1000L*60*60*24;
        long now = System.currentTimeMillis();
        if(p.toFile().lastModified() > (now-millis))
        {
            return true;
        }
        return false;
    }

    public static String readAll(final InputStream in) throws IOException
    {
        return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
}
