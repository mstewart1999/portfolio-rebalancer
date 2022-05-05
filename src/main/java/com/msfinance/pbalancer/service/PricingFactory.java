package com.msfinance.pbalancer.service;

public class PricingFactory
{
    public static IPricing get()
    {
        if(System.getProperty("pb.PricingFactory", "mock").equals("mock")) // TODO
        {
            return new MockPricing();
        }
        return new CentralPricing();
    }
}
