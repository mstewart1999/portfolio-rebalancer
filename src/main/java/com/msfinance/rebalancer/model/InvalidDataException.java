package com.msfinance.rebalancer.model;

public class InvalidDataException extends Exception
{
    private static final long serialVersionUID = 1899575742296872131L;

    public InvalidDataException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public InvalidDataException(final String message)
    {
        super(message);
    }

}
