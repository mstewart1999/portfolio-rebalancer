package com.msfinance.rebalancer.model;

import com.msfinance.rebalancer.model.aa.AssetAllocation;

public record Asset(String id, String nm, AssetAllocation allocation)
{
}
