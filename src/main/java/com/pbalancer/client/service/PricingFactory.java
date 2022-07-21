package com.pbalancer.client.service;

import com.pbalancer.client.Config;

public class PricingFactory
{
    private enum Impl { MOCK, CENTRAL }

    public static IPricing get()
    {
        Impl impl = Impl.valueOf(Config.getInstance().getPricingImpl());
        switch(impl)
        {
        case MOCK: return new MockPricing();
        case CENTRAL: return new CentralPricing();
        default: throw new IllegalArgumentException("Unhandled enum " + impl);
        }
    }
}
