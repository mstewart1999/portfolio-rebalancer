package com.msfinance.rebalancer.model.aa;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class TestPresetAA
{

    @Test
    void test() throws IOException
    {
        for(PresetAA aa : PresetAA.values())
        {
            System.out.println(aa.getAA().toJson());
        }
    }

}
