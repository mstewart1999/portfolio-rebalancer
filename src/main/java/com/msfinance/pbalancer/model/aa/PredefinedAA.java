package com.msfinance.pbalancer.model.aa;

import java.io.IOException;
import java.io.InputStream;

import com.msfinance.pbalancer.util.JSONHelper;

public enum PredefinedAA
{
  // Portfolios: https://portfoliocharts.com/portfolios/
  // compare to https://portfolioslab.com/lazy-portfolios
    // https://www.portfolioeinstein.com
    // http://www.lazyportfolioetf.com
    THREE_FUND(
            "Three-Fund Portfolio (60/40)",
            "https://portfoliocharts.com/portfolio/three-fund-portfolio/"
            ),
    TSM(
            "Total Stock Market Portfolio",
            "https://portfoliocharts.com/portfolio/total-stock-market/"
            ),
    CLASSIC_60_40(
            "Classic 60-40 Portfolio",
            "https://portfoliocharts.com/portfolio/classic-60-40/"
            ),
    SEVEN_TWELVE(
            "7Twelve Portfolio",
            "https://portfoliocharts.com/portfolio/7twelve-portfolio/"
            ),

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

    public static final String PREDEFINED_HELP_URL = PredefinedAA.class.getResource("/help/targetAA-predefined.html").toString();
    public static final String CUSTOM_HELP_URL = PredefinedAA.class.getResource("/help/targetAA-custom.html").toString();

    private String text;
    private String url;

    PredefinedAA(final String text, final String url) throws RuntimeException
    {
        this.text = text;
        this.url = url;
    }

    public String getText()
    {
        return text;
    }

    public String getUrl()
    {
        return url;
    }

    public AssetAllocation getAA()
    {
        // lazy load, each invocation gets its own copy
        String fileNm = this.name() + ".json";

        // find the predefined data file as a resource relative to AA class
        try(InputStream in = AssetAllocation.class.getResourceAsStream(fileNm))
        {
            String content = JSONHelper.readAll(in);
            AssetAllocation aa = JSONHelper.fromJson(content, AssetAllocation.class);
            aa.setPredefined(this);
            return aa;
        }
        catch(IOException e)
        {
            throw new RuntimeException("Unable to load PresetAA=" + name(), e);
        }
    }

}
