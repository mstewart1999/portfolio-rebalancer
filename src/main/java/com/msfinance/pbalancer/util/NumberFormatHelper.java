package com.msfinance.pbalancer.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class NumberFormatHelper
{
    private static BigDecimal BILLION = new BigDecimal("1000000000");
    private static BigDecimal MILLION = new BigDecimal("1000000");
    private static BigDecimal THOUSAND = new BigDecimal("1000");


    public static String prettyFormatCurrency(BigDecimal value)
    {
        if(value == null)
        {
            return "";
        }
        DecimalFormat df;
        String suffix;
        // TODO: eventually care about portfolios larger than 1B
        if(value.doubleValue() >= BILLION.doubleValue())
        {
            value = value.divide(BILLION);
            suffix = " B";
            df = new DecimalFormat(significantDigit4Format(value));
        }
        else if(value.doubleValue() >= MILLION.doubleValue())
        {
            value = value.divide(MILLION);
            suffix = " M";
            df = new DecimalFormat(significantDigit4Format(value));
        }
        else if(value.doubleValue() >= THOUSAND.doubleValue())
        {
            value = value.divide(THOUSAND);
            suffix = " K";
            df = new DecimalFormat(significantDigit4Format(value));
        }
        else
        {
            suffix = "";
            // assume that for pretty values, no one cares about cents
            // comma should never come into play
            df = new DecimalFormat("#,##0");
        }

        return df.format(value) + suffix;
    }

    private static String significantDigit4Format(final BigDecimal value)
    {
        if(value.doubleValue() >= THOUSAND.doubleValue())
        {
            return "#,##0";
        }
        else if(value.doubleValue() >= 100)
        {
            return "##0.0";
        }
        else if(value.doubleValue() >= 10)
        {
            return "#0.00";
        }
        else if(value.doubleValue() >= 1)
        {
            return "0.000";
        }
        else
        {
            return "0.000";
        }
    }

    public static BigDecimal sum(final BigDecimal v1, final BigDecimal v2)
    {
        if((v1 == null) && (v2 == null))
        {
            return BigDecimal.ZERO;
        }
        if(v1 == null)
        {
            return v2;
        }
        if(v2 == null)
        {
            return v1;
        }
        return v1.add(v2);
    }

    public static String formatWith4Decimals(final BigDecimal val)
    {
        if(val == null)
        {
            return "";
        }

        return new DecimalFormat("#,##0.0000").format(val);
    }

    public static String formatWith3Decimals(final BigDecimal val)
    {
        if(val == null)
        {
            return "";
        }

        return new DecimalFormat("#,##0.000").format(val);
    }

    public static String formatWith2Decimals(final BigDecimal val)
    {
        if(val == null)
        {
            return "";
        }

        return new DecimalFormat("#,##0.00").format(val);
    }

    public static BigDecimal parseNumber4(final String val)
    {
        return parseNumber(val, 4);
    }
    public static BigDecimal parseNumber3(final String val)
    {
        return parseNumber(val, 3);
    }
    public static BigDecimal parseNumber2(final String val)
    {
        return parseNumber(val, 2);
    }
    private static BigDecimal parseNumber(String val, final int decimals)
    {
        if(Validation.isBlank(val))
        {
            return null;
        }

        try
        {
            if(val.startsWith("$"))
            {
                val = val.substring(1);
            }
            if(val.contains(","))
            {
                // BigDecimal does not deal with commas
                // Europeans might cringe that this isn't localized with thousands separator
                val = val.replace(",", "");
            }
            val = val.strip();
            return new BigDecimal(val).setScale(decimals, RoundingMode.HALF_UP);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
}
