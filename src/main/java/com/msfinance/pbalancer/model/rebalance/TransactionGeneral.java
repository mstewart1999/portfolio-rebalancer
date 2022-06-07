package com.msfinance.pbalancer.model.rebalance;

import java.util.List;

import com.msfinance.pbalancer.model.Asset;

/**
 * Temporary placeholder data for invest suggestion algorithm.
 */
public record TransactionGeneral(String assetClass, List<Asset> possibleAssets, double howMuchDollars)
{
}
