package com.pbalancer.client.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.pbalancer.client.model.PriceResult;

public class MockPricing implements IPricing
{
    private static Map<String,PriceResult> mockData = new HashMap<>();
    static
    {
        Date when = new Date(LocalDate.parse("2022-05-04").atTime(16, 0).atZone(ZoneId.of("America/New_York")).toInstant().toEpochMilli());

        mockData.put("VTAPX", new PriceResult("VTAPX", new BigDecimal("25.29"), when));
        mockData.put("VTABX", new PriceResult("VTABX", new BigDecimal("20.30"), when));
        mockData.put("VSBSX", new PriceResult("VSBSX", new BigDecimal("19.63"), when));
        mockData.put("AVUV", new PriceResult("AVUV", new BigDecimal("77.16"), when));
        mockData.put("AVDV", new PriceResult("AVDV", new BigDecimal("59.19"), when));
        mockData.put("AVES", new PriceResult("AVES", new BigDecimal("46.27"), when));
        mockData.put("VXUS", new PriceResult("VXUS", new BigDecimal("55.68"), when));

        mockData.put("VWLUX", new PriceResult("VWLUX", new BigDecimal("10.79"), when));
        mockData.put("VTSAX", new PriceResult("VTSAX", new BigDecimal("101.94"), when));
        mockData.put("VTIAX", new PriceResult("VTIAX", new BigDecimal("30.16"), when));
        mockData.put("VSIAX", new PriceResult("VSIAX", new BigDecimal("72.18"), when));
        mockData.put("LUV", new PriceResult("LUV", new BigDecimal("47.32"), when));

        mockData.put("VAIPX", new PriceResult("VAIPX", new BigDecimal("26.47"), when));
        mockData.put("VWETX", new PriceResult("VWETX", new BigDecimal("8.66"), when));
        mockData.put("FNDF", new PriceResult("FNDF", new BigDecimal("30.70"), when));

        mockData.put("COF", new PriceResult("COF", new BigDecimal("128.98"), when));
        mockData.put("VTIP", new PriceResult("VTIP", new BigDecimal("50.38"), when));

        mockData.put("IWN", new PriceResult("IWN", new BigDecimal("150.72"), when));
        mockData.put("VEU", new PriceResult("VEU", new BigDecimal("53.72"), when));
        mockData.put("VSS", new PriceResult("VSS", new BigDecimal("113.86"), when));
        mockData.put("VWO", new PriceResult("VWO", new BigDecimal("43.08"), when));
        mockData.put("VTI", new PriceResult("VTI", new BigDecimal("208.42"), when));

        // experimental proxy asset
        /*
        mockData.put("P-VEA",            new PriceResult("P-VEA",            new BigDecimal("17.111"), when));
        mockData.put("P-VEA-2021-11-01", new PriceResult("P-VEA-2021-11-01", new BigDecimal("20.253"), when));
        mockData.put("VEA",              new PriceResult("VEA",              new BigDecimal("44.71"), when));
        mockData.put("VEA-2021-11-01",   new PriceResult("VEA-2021-11-01",  new BigDecimal("52.92"), when));
        */

        // data for a test
        when = new Date(LocalDate.parse("2022-05-05").atTime(16, 0).atZone(ZoneId.of("America/New_York")).toInstant().toEpochMilli());
        mockData.put("ACWX", new PriceResult("ACWX", new BigDecimal("47.66"), when)); // for proxy
        mockData.put("VFINX", new PriceResult("VFINX", new BigDecimal("383.11"), when)); // for proxy
        mockData.put("VTTVX", new PriceResult("VTTVX", new BigDecimal("17.99"), when));
        mockData.put("PLRIX", new PriceResult("PLRIX", new BigDecimal("8.06"), when));
        mockData.put("FIPDX", new PriceResult("FIPDX", new BigDecimal("10.45"), when));

        mockData.put("VTMFX", new PriceResult("VTMFX", new BigDecimal("37.78"), when)); // for proxy
        mockData.put("BND", new PriceResult("BND", new BigDecimal("75.57"), when)); // for proxy

        // for SAMPLE portfolio
        when = new Date(LocalDate.parse("2022-06-03").atTime(16, 0).atZone(ZoneId.of("America/New_York")).toInstant().toEpochMilli());
        mockData.put("FXAIX", new PriceResult("FXAIX", new BigDecimal("143.02"), when)); // S&P 500
        mockData.put("FXNAX", new PriceResult("FXNAX", new BigDecimal("10.78"), when)); // bond index
        mockData.put("IIRLX", new PriceResult("IIRLX", new BigDecimal("28.03"), when)); // Voya Russell Large Cap Index Portfolio - Class I (IIRLX)
        mockData.put("IPIIX", new PriceResult("IPIIX", new BigDecimal("11.36"), when)); // Voya Intermediate Bond Portfolio - Class I (IPIIX)
    }

    @Override
    public Map<String,PriceResult> getMostRecentEOD(final Collection<String> tickers)
    {
        Map<String,PriceResult> out = new HashMap<>();
        for(String ticker : tickers)
        {
            if(mockData.containsKey(ticker))
            {
                out.put(ticker, mockData.get(ticker));
            }
        }
        return out;
    }

}
