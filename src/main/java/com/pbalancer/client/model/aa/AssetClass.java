package com.pbalancer.client.model.aa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AssetClass
{
    public static final String UNDEFINED = "Undefined";
    public static final String CASH = "FI-CASH";

    private static final Map<String,AssetClass> KNOWN = new HashMap<>();
    private static final List<AssetClass> KNOWN_LIST = new ArrayList<>();

    static
    {
        String fileNm = "AssetClassList.psv";

        // find the predefined data file as a resource relative to AA class
        try(InputStream in = AssetClass.class.getResourceAsStream(fileNm))
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            for(String line : br.lines().collect(Collectors.toList()))
            {
                if(!line.isBlank())
                {
                    String[] fields = line.strip().split(Pattern.quote("|"));
                    String longDescription = (fields.length >= 3) ? fields[2] : "";
                    new AssetClass(true, fields[0], fields[1], longDescription);
                }
            }
        }
        catch(IOException|NullPointerException|ArrayIndexOutOfBoundsException e)
        {
            throw new RuntimeException("Unable to load asset classes from: " + fileNm, e);
        }

        new AssetClass(true, UNDEFINED, "TBD", "");
    }

    public static final void add(final String cd)
    {
        if(!KNOWN.containsKey(cd))
        {
            AssetClass ac = new AssetClass(false, cd, "Custom", "??");
            KNOWN.put(cd, ac);
            KNOWN_LIST.add(ac);
        }
    }

    public static AssetClass lookup(final String cd)
    {
        return KNOWN.get(cd);
    }

    public static List<AssetClass> list()
    {
        return Collections.unmodifiableList(KNOWN_LIST);
    }

    public static Map<String,AssetClass> all()
    {
        return Collections.unmodifiableMap(KNOWN);
    }

    //----------------------------------------
    private final boolean builtIn;
    private final String code;
    private final String shortDescription;
    private final String longDescription;

    private AssetClass(final boolean builtIn, final String code, final String shortDescription, final String longDescription)
    {
        this.builtIn = builtIn;
        this.code = code;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;

        if(KNOWN.containsKey(code))
        {
            throw new IllegalStateException("Attempted to create asset class twice: " + code);
        }
        KNOWN.put(code, this);
        KNOWN_LIST.add(this);
    }

    public boolean isBuiltIn()
    {
        return builtIn;
    }

    public String getCode()
    {
        return code;
    }

    public String getShortDescription()
    {
        return shortDescription;
    }

    public String getLongDescription()
    {
        return longDescription;
    }


    public static boolean isEquity(final String code)
    {
        return code.startsWith("EQ-");
    }

}
