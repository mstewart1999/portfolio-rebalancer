package com.msfinance.pbalancer.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class TestNumberFormatHelper
{

    @Test
    void testPrettyFormatCurrency()
    {
        assertEquals("0", NumberFormatHelper.prettyFormatCurrency(new BigDecimal("0.01")));
        assertEquals("0", NumberFormatHelper.prettyFormatCurrency(new BigDecimal("0.10")));
        assertEquals("1", NumberFormatHelper.prettyFormatCurrency(new BigDecimal("1.00")));
        assertEquals("10", NumberFormatHelper.prettyFormatCurrency(new BigDecimal("10.00")));
        assertEquals("100", NumberFormatHelper.prettyFormatCurrency(new BigDecimal("100.00")));
        assertEquals("1.000 K", NumberFormatHelper.prettyFormatCurrency(new BigDecimal("1000.00")));
        assertEquals("10.00 K", NumberFormatHelper.prettyFormatCurrency(new BigDecimal("10000.00")));
        assertEquals("100.0 K", NumberFormatHelper.prettyFormatCurrency(new BigDecimal("100000.00")));
        assertEquals("1.000 M", NumberFormatHelper.prettyFormatCurrency(new BigDecimal("1000000.00")));
        assertEquals("10.00 M", NumberFormatHelper.prettyFormatCurrency(new BigDecimal("10000000.00")));
        assertEquals("100.0 M", NumberFormatHelper.prettyFormatCurrency(new BigDecimal("100000000.00")));
        assertEquals("1.000 B", NumberFormatHelper.prettyFormatCurrency(new BigDecimal("1000000000.00")));
    }

    @Test
    void testSum()
    {
        assertEquals("0", NumberFormatHelper.sum(null, null).toPlainString());
        assertEquals("1", NumberFormatHelper.sum(null, new BigDecimal("1")).toPlainString());
        assertEquals("1", NumberFormatHelper.sum(new BigDecimal("1"), null).toPlainString());
        assertEquals("2", NumberFormatHelper.sum(new BigDecimal("1"), new BigDecimal("1")).toPlainString());
    }

    @Test
    void testFormatWith4Decimals()
    {
        assertEquals("", NumberFormatHelper.formatWith4Decimals(null));
        assertEquals("123,456,789.0000", NumberFormatHelper.formatWith4Decimals(new BigDecimal("123456789")));
        assertEquals("123,456,789.1234", NumberFormatHelper.formatWith4Decimals(new BigDecimal("123456789.12345")));
    }

    @Test
    void testFormatWith3Decimals()
    {
        assertEquals("", NumberFormatHelper.formatWith3Decimals(null));
        assertEquals("123,456,789.000", NumberFormatHelper.formatWith3Decimals(new BigDecimal("123456789")));
        assertEquals("123,456,789.234", NumberFormatHelper.formatWith3Decimals(new BigDecimal("123456789.2345")));
    }

    @Test
    void testFormatWith2Decimals()
    {
        assertEquals("", NumberFormatHelper.formatWith2Decimals(null));
        assertEquals("123,456,789.00", NumberFormatHelper.formatWith2Decimals(new BigDecimal("123456789")));
        assertEquals("123,456,789.34", NumberFormatHelper.formatWith2Decimals(new BigDecimal("123456789.345")));
    }

    @Test
    void testParseNumber4()
    {
        assertEquals(null, NumberFormatHelper.parseNumber4(null));
        assertEquals(null, NumberFormatHelper.parseNumber4(""));
        assertEquals(null, NumberFormatHelper.parseNumber4(" "));
        assertEquals("123456789.0000", NumberFormatHelper.parseNumber4(" 123456789 ").toPlainString());
        assertEquals("123456789.1235", NumberFormatHelper.parseNumber4("123456789.12345").toPlainString());
    }

    @Test
    void testParseNumber3()
    {
        assertEquals(null, NumberFormatHelper.parseNumber3(null));
        assertEquals(null, NumberFormatHelper.parseNumber3(""));
        assertEquals(null, NumberFormatHelper.parseNumber3(" "));
        assertEquals("123456789.000", NumberFormatHelper.parseNumber3(" 123456789 ").toPlainString());
        assertEquals("123456789.235", NumberFormatHelper.parseNumber3("123456789.2345").toPlainString());
    }

    @Test
    void testParseNumber2()
    {
        assertEquals(null, NumberFormatHelper.parseNumber2(null));
        assertEquals(null, NumberFormatHelper.parseNumber2(""));
        assertEquals(null, NumberFormatHelper.parseNumber2(" "));
        assertEquals("123456789.00", NumberFormatHelper.parseNumber2(" 123456789 ").toPlainString());
        assertEquals("123456789.35", NumberFormatHelper.parseNumber2("123456789.345").toPlainString());
    }

}
