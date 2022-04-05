package com.msfinance.pbalancer.model.aa;

import java.io.IOException;
import java.io.InputStream;

import com.msfinance.pbalancer.util.JSONHelper;

public enum PresetAA
{
  // Portfolios: https://portfoliocharts.com/portfolios/
  // compare to https://portfolioslab.com/lazy-portfolios
    // https://www.portfolioeinstein.com
    // http://www.lazyportfolioetf.com
    THREE_FUND,
    TSM,
    CLASSIC_60_40,
    SEVEN_TWELVE,
// TODO
//    ALL_SEASONS,
//    COFFEEHOUSE,
//    CORE_FOUR,
//    GLOBAL_MARKET,
//    GOLDEN_BUTTERFLY,
//    IDEAL_INDEX,
//    IVY,
//    LARRY,
//    MERRIMAN_ULTIMATE,
//    NO_BRAINER,
//    PERMANENT,
//    SANDWICH,
//    SWENSEN,
    // BUILD YOUR OWN??
    ;

    private AssetAllocation aa;

    PresetAA() throws RuntimeException
    {
        String fileNm = this.name() + ".json";

        // find the predefined data file as a resource relative to AA class
        try(InputStream in = AssetAllocation.class.getResourceAsStream(fileNm))
        {
            String content = JSONHelper.readAll(in);
            this.aa = JSONHelper.fromJson(content, AssetAllocation.class);
        }
        catch(IOException e)
        {
            throw new RuntimeException("Unable to load PresetAA=" + name(), e);
        }
    }

    public AssetAllocation getAA()
    {
        return aa;
    }
}
