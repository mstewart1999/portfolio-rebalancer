package com.pbalancer.client.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.pbalancer.client.model.Asset;

class TestAsset
{

    @Test
    void testTotalValue()
    {
        assertEquals(null, Asset.totalValue(null, null));
        assertEquals(null, Asset.totalValue(null, new BigDecimal("1")));
        assertEquals(null, Asset.totalValue(new BigDecimal("1"), null));
        assertEquals("6.10", Asset.totalValue(new BigDecimal("1.123"), new BigDecimal("5.4321")).toPlainString());
    }

}
