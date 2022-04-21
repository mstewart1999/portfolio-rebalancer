package com.msfinance.pbalancer.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;

public class NumberFormatHelper
{
    private static BigDecimal MILLION = new BigDecimal("1000000");
    private static BigDecimal THOUSAND = new BigDecimal("1000");

    public static MathContext CURRENCY2_MATH_CONTEXT = new MathContext(2);
    public static MathContext CURRENCY3_MATH_CONTEXT = new MathContext(3);
    public static MathContext CURRENCY4_MATH_CONTEXT = new MathContext(4);


    public static String prettyFormatCurrency(BigDecimal value)
    {
        if(value == null)
        {
            return "";
        }
        DecimalFormat df;
        String suffix;
        // TODO: eventually care about portfolios larger than 1B
        if(value.doubleValue() > MILLION.doubleValue())
        {
            value = value.divide(MILLION);
            suffix = " M";
            df = new DecimalFormat(significantDigit4Format(value));
        }
        else if(value.doubleValue() > THOUSAND.doubleValue())
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
        if(value.doubleValue() > THOUSAND.doubleValue())
        {
            return "#,##0";
        }
        else if(value.doubleValue() > 100)
        {
            return "##0.0";
        }
        else if(value.doubleValue() > 10)
        {
            return "#0.00";
        }
        else if(value.doubleValue() > 1)
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

    public static BigDecimal parseNumber2(final String val)
    {
        return parseNumber(val, CURRENCY2_MATH_CONTEXT);
    }
    public static BigDecimal parseNumber3(final String val)
    {
        return parseNumber(val, CURRENCY3_MATH_CONTEXT);
    }
    public static BigDecimal parseNumber4(final String val)
    {
        return parseNumber(val, CURRENCY4_MATH_CONTEXT);
    }
    public static BigDecimal parseNumber(String val, final MathContext mc)
    {
        if(Validation.isBlank(val))
        {
            return null;
        }

        try
        {
            if(val.contains(","))
            {
                // BigDecimal does not deal with commas
                val = val.replace(",", "");
            }
            return new BigDecimal(val, mc);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
}
