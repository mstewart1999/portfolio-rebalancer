package com.pbalancer.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Version
{
    private static final String NAME = "/version.properties";
    private final Properties props = new Properties();

    public Version()
    {
        InputStream inputStream = getClass().getResourceAsStream(NAME);

        if (inputStream == null)
        {
            throw new RuntimeException("version property file '" + NAME + "' not found in the classpath");
        }

        try(Reader r = new InputStreamReader(inputStream, StandardCharsets.UTF_8))
        {
            props.load(r);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to read version property file '" + NAME + "'");
        }
    }

    public String getVersion()
    {
        return props.getProperty("version", "UNDEFINED");
    }

    public String getBuildNbr()
    {
        return props.getProperty("buildNbr", "UNDEFINED");
    }

    public String getBuildDate()
    {
        return props.getProperty("buildDate", "UNDEFINED");
    }
}
