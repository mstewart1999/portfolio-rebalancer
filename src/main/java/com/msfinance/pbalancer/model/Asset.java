package com.msfinance.pbalancer.model;

import com.msfinance.pbalancer.model.aa.AssetAllocation;

public record Asset(String id, String nm, AssetAllocation allocation)
{
}
