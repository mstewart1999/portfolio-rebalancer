package com.msfinance.pbalancer.util;

import java.util.Objects;

import com.msfinance.pbalancer.model.InvalidDataException;

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

    public static void assertTrue(final boolean b)
    {
        if(!b) throw new IllegalArgumentException();
    }

    public static <T> boolean isSame(final T o1, final T o2)
    {
        if((o1 == null) && (o2 == null))
        {
            return true;
        }
        if(o1 == null)
        {
            return false;
        }
        if(o2 == null)
        {
            return false;
        }
        return o1.equals(o2);
    }
}
