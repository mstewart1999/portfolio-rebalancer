package com.pbalancer.client.service;

public class ServiceException extends Exception
{
    private static final long serialVersionUID = 1L;
    public ServiceException(final String msg, final Exception parent)
    {
        super(msg, parent);
    }
    public ServiceException(final String msg)
    {
        super(msg);
    }
}
