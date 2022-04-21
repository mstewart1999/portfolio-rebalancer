package com.msfinance.pbalancer.model.aa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import com.msfinance.pbalancer.model.InvalidDataException;

public enum PredefinedAA
{
  // Portfolios: https://portfoliocharts.com/portfolios/
  // compare to https://portfolioslab.com/lazy-portfolios
    // https://www.portfolioeinstein.com
    // http://www.lazyportfolioetf.com
    // https://www.optimizedportfolio.com/
    THREE_FUND(
            "Three-Fund Portfolio (60/40)",
            "https://portfoliocharts.com/portfolio/three-fund-portfolio/"
            ),

    ALL_SEASONS(
            "All Seasons Portfolio",
            "https://portfoliocharts.com/portfolio/all-seasons-portfolio/"),
    CLASSIC_60_40(
            "Classic 60-40 Portfolio",
            "https://portfoliocharts.com/portfolio/classic-60-40/"
            ),
    COFFEEHOUSE(
            "Coffeehouse Portfolio",
            "https://portfoliocharts.com/portfolio/coffeehouse-portfolio/"),
    CORE_FOUR(
            "Core Four Portfolio (Moderate Growth - Classic)",
            "https://portfoliocharts.com/portfolio/rick-ferri-core-four/"),
    GLOBAL_MARKET(
            "Global Market Portfolio",
            "https://portfoliocharts.com/portfolio/global-market-portfolio/"),
    GOLDEN_BUTTERFLY(
            "Golden Butterfly Portfolio",
            "https://portfoliocharts.com/portfolio/golden-butterfly/"),
    IDEAL_INDEX(
            "Ideal Index Portfolio",
            "https://portfoliocharts.com/portfolio/ideal-index-portfolio/"),
    IVY(
            "Ivy Portfolio",
            "https://portfoliocharts.com/portfolio/ivy-portfolio/"),
    LARRY(
            "Larry Portfolio",
            "https://portfoliocharts.com/portfolio/larry-portfolio/"),
    MERRIMAN_ULTIMATE(
            "Merriman Ultimate Portfolio",
            "https://portfoliocharts.com/portfolio/merriman-ultimate/"),
    NO_BRAINER(
            "No-Brainer Portfolio",
            "https://portfoliocharts.com/portfolio/no-brainer-portfolio/"),
    PERMANENT(
            "Permanent Portfolio",
            "https://portfoliocharts.com/portfolio/permanent-portfolio/"),
    PINWHEEL(
            "Pinwheel Portfolio",
            "https://portfoliocharts.com/portfolio/pinwheel-portfolio/"),
    SANDWICH(
            "Sandwich Portfolio",
            "https://portfoliocharts.com/portfolio/sandwich-portfolio/"),
    SEVEN_TWELVE(
            "7Twelve Portfolio",
            "https://portfoliocharts.com/portfolio/7twelve-portfolio/"
            ),
    SWENSEN(
            "Swensen Portfolio",
            "https://portfoliocharts.com/portfolio/swensen-portfolio/"),
    TSM(
            "Total Stock Market Portfolio",
            "https://portfoliocharts.com/portfolio/total-stock-market/"
            ),
    ;

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
        String fileNm = this.name() + ".csv";

        // find the predefined data file as a resource relative to this class
        try(InputStream in = this.getClass().getResourceAsStream(fileNm))
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            List<String> lines = br.lines().collect(Collectors.toList());
            AssetAllocation aa = new AssetAllocation(this, lines);
            return aa;
        }
        catch(IOException|InvalidDataException|NullPointerException e)
        {
            throw new RuntimeException("Unable to load PresetAA=" + name(), e);
        }
    }

}
