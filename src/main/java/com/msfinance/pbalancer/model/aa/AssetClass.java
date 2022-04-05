package com.msfinance.pbalancer.model.aa;

import java.util.HashSet;
import java.util.Set;

public class AssetClass
{
    public static final String UNALLOCATED = "Unallocated";

    private static final Set<String> KNOWN = new HashSet<>();

    static
    {
        add("EQ-US-TSM");
        add("EQ-ID-TSM");
        add("EQ-IE-TSM");
        add("EQ-IG-TSM");
        add("EQ-TW-TSM");
        add("EQ-US", new String[]{"LV", "LB", "LG", "MV", "MB", "MG", "SV", "SB", "SG"});
        add("EQ-ID", new String[]{"LV", "LB", "LG", "MV", "MB", "MG", "SV", "SB", "SG"});
        add("EQ-IE", new String[]{"LV", "LB", "LG", "MV", "MB", "MG", "SV", "SB", "SG"});
        add("EQ-IG", new String[]{"LV", "LB", "LG", "MV", "MB", "MG", "SV", "SB", "SG"});

        add("FI-CASH");
        add("FI-F-TBM");
        add("FI-ST-ANY");
        add("FI-IT-ANY");
        add("FI-LT-ANY");
        add("FI-IT-TBM");
        add("FI-IT-TIBM"); // TODO: regions?
        add("FI-ST", new String[]{"MUN", "CORP", "GOV", "IPS"});
        add("FI-IT", new String[]{"MUN", "CORP", "GOV", "IPS"});
        add("FI-LT", new String[]{"MUN", "CORP", "GOV", "IPS"});

        add("OTHER-COM-CCF");
        add("OTHER-COM-NR");
        add("OTHER-REIT-US");

        add(UNALLOCATED);
    }

    public static final void add(final String nm)
    {
        KNOWN.add(nm);
    }

    public static final void add(final String nmPrefix, final String[] suffixes)
    {
        for(String suffix : suffixes)
        {
            KNOWN.add(nmPrefix + "-" + suffix);
        }
    }

    public static boolean exists(final String nm)
    {
        return KNOWN.contains(nm);
    }
}
