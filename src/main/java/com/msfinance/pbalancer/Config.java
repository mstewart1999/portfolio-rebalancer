package com.msfinance.pbalancer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Config
{
    private static final String NAME = "/pBalancer.properties";
    private static Config config;

    public static synchronized Config getInstance()
    {
        if(config == null)
        {
            config = new Config();
        }
        return config;
    }

    private final Properties props = new Properties();

    private Config()
    {
        InputStream inputStream = getClass().getResourceAsStream(NAME);

        if (inputStream == null)
        {
            throw new RuntimeException("config property file '" + NAME + "' not found in the classpath");
        }

        try(Reader r = new InputStreamReader(inputStream, StandardCharsets.UTF_8))
        {
            props.load(r);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to read config property file '" + NAME + "'");
        }
    }

    public String getCentralURL()
    {
        return props.getProperty("pBalancer.central.url", "http://localhost:8080/");
    }

    public String getCentralUser()
    {
        return props.getProperty("pBalancer.central.user", "");
    }

    public String getCentralPass()
    {
        return props.getProperty("pBalancer.central.pass", "");
    }
}
