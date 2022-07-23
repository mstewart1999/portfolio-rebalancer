package com.pbalancer.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import com.pbalancer.client.util.Validation;

public class Config
{
    private static Config config;

    public static synchronized Config getInstance()
    {
        if(config == null)
        {
            config = new Config();
        }
        return config;
    }

    private final Properties installedProps = new Properties();
    private final Properties baseProps = new Properties();
    private final Properties envProps = new Properties();

    private Config()
    {
        loadFromFile(installedProps, new File(SystemSettings.getPBalancerDataDir(), "pBalancer.properties"));
        loadFromResource(baseProps, "/pBalancer.properties");
        loadFromResource(envProps, "/pBalancer-" + Environment.getActive().name().toLowerCase() + ".properties");
    }

    private void loadFromFile(final Properties props, final File file)
    {
        if (!file.exists())
        {
            // this is OK, just keep an empty set of properties
            return;
        }

        try(Reader r = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))
        {
            props.load(r);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to read config property file '" + file.getAbsolutePath() + "'");
        }
    }

    private void loadFromResource(final Properties props, final String name)
    {
        InputStream inputStream = getClass().getResourceAsStream(name);

        if (inputStream == null)
        {
            throw new RuntimeException("config property resource '" + name + "' not found in the classpath");
        }

        try(Reader r = new InputStreamReader(inputStream, StandardCharsets.UTF_8))
        {
            props.load(r);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to read config property resource '" + name + "'");
        }
    }

    private String getValue(final String key, final String defaultValue)
    {
        if(!Validation.isBlank(installedProps.getProperty(key)))
        {
            return installedProps.getProperty(key);
        }
        if(!Validation.isBlank(envProps.getProperty(key)))
        {
            return envProps.getProperty(key);
        }
        if(!Validation.isBlank(baseProps.getProperty(key)))
        {
            return baseProps.getProperty(key);
        }
        return defaultValue;
    }


    public String getDataImpl()
    {
        return getValue("pBalancer.data.impl", "");
    }

    public String getPricingImpl()
    {
        return getValue("pBalancer.pricing.impl", "");
    }

    public String getCentralURL()
    {
        return getValue("pBalancer.central.url", "");
    }

    public String getCentralUser()
    {
        return getValue("pBalancer.central.user", "");
    }

    public String getCentralPass()
    {
        return getValue("pBalancer.central.pass", "");
    }
}
