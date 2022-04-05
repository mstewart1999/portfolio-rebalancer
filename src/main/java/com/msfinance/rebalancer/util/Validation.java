package com.msfinance.rebalancer.util;

import java.util.Objects;

import com.msfinance.rebalancer.model.InvalidDataException;

public class Validation
{
    public static boolean almostEqual(final double a, final double b, final double eps)
    {
        return Math.abs(a-b)<eps;
    }

    public static boolean isBlank(final String val)
    {
        return (val == null) || val.isBlank();
    }

    public static <T> void assertNull(final T o) throws IllegalArgumentException
    {
        if(o != null) throw new IllegalArgumentException();
    }

    public static <T> T assertNonNull(final T o) throws NullPointerException
    {
        return Objects.requireNonNull(o);
    }

    public static int toInt(final String val) throws InvalidDataException
    {
        try
        {
            return Integer.valueOf(val);
        }
        catch(NumberFormatException|NullPointerException e)
        {
            throw new InvalidDataException("Invalid int", e);
        }
    }
}
