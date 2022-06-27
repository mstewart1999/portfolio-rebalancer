package com.msfinance.pbalancer.model.aa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPreferredAssetCache
{
    private static final Logger LOG = LoggerFactory.getLogger(DefaultPreferredAssetCache.class);

    private static final String RESOURCE_NAME = "DefaultPreferredAsset.csv";

    private static DefaultPreferredAssetCache instance = null;

    public static synchronized DefaultPreferredAssetCache getInstance()
    {
        if(instance == null)
        {
            instance = new DefaultPreferredAssetCache();
            instance.load();
        }
        return instance;
    }

    private final Map<String,DefaultPreferredAsset> primary = new TreeMap<>();
    private final Map<String,List<DefaultPreferredAsset>> choices = new TreeMap<>();

    private DefaultPreferredAssetCache()
    {
    }

    private void load()
    {
        try(BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(RESOURCE_NAME), StandardCharsets.UTF_8)))
        {
            br.lines()
                .skip(1)
                .filter(l -> !l.startsWith("#"))
                .filter(l -> !l.isBlank())
                .map(l -> l.split(Pattern.quote(",")))
                .filter(arr -> arr.length >= 3)
                .map(arr -> new DefaultPreferredAsset(arr))
                .filter(dpa -> dpa.isKeeper())
                .forEach(dpa -> add(dpa))
                ;
        }
        catch(IOException|NullPointerException|ArrayIndexOutOfBoundsException e)
        {
            throw new RuntimeException("Unable to DefaultPreferredAssets from: " + RESOURCE_NAME, e);
        }

        // sort each list of choices
        for(List<DefaultPreferredAsset> dpa : choices.values())
        {
            dpa.sort(Comparator.comparing(DefaultPreferredAsset::getTicker));
        }

        for(String ac : AssetClass.all().keySet())
        {
            if(!AssetClass.UNDEFINED.equals(ac))
            {
                if(!primary.containsKey(ac))
                {
                    LOG.warn(RESOURCE_NAME + " contains no primary for " + ac);
                }
                if(!choices.containsKey(ac))
                {
                    choices.put(ac, new ArrayList<>());
                }
            }
        }

    }

    private void add(final DefaultPreferredAsset dpa)
    {
        String ac = dpa.getAssetClass();
        if(dpa.isPrimary())
        {
            if(primary.containsKey(ac))
            {
                LOG.error(RESOURCE_NAME + " contains duplicate primary for " + ac);
            }
            primary.put(ac, dpa);
        }

        if(!choices.containsKey(ac))
        {
            choices.put(ac, new ArrayList<>());
        }
        boolean found = false;
        for(DefaultPreferredAsset item : choices.get(ac))
        {
            if(item.getTicker().equals(dpa.getTicker()))
            {
                found = true;
                LOG.debug(RESOURCE_NAME + " contains duplicate for " + ac + "," + dpa.getTicker());
                if(dpa.isPrimary() && !item.isPrimary())
                {
                    LOG.error(RESOURCE_NAME + " OOPS - put primary first in file " + ac + "," + dpa.getTicker());
                }
            }
        }
        if(!found)
        {
            choices.get(ac).add(dpa);
        }
    }


    public DefaultPreferredAsset lookupPrimary(String assetClass)
    {
        if(assetClass == null)
        {
            assetClass = "";
        }
        return primary.get(assetClass);
    }

    public List<DefaultPreferredAsset> lookupChoices(String assetClass)
    {
        if(assetClass == null)
        {
            assetClass = "";
        }
        List<DefaultPreferredAsset> dpas = choices.get(assetClass);
        if(dpas == null)
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(dpas);
    }



}
