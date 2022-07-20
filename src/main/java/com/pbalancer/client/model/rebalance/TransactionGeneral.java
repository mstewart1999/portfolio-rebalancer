package com.pbalancer.client.model.rebalance;

import java.util.List;

import com.pbalancer.client.model.Asset;

/**
 * Temporary placeholder data for invest suggestion algorithm.
 */
public record TransactionGeneral(String assetClass, List<Asset> possibleAssets, double howMuchDollars)
{
}
