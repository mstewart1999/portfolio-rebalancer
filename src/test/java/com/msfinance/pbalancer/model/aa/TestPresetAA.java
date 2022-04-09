package com.msfinance.pbalancer.model.aa;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.msfinance.pbalancer.model.aa.PredefinedAA;

class TestPresetAA
{

    @Test
    void test() throws IOException
    {
        for(PredefinedAA aa : PredefinedAA.values())
        {
            System.out.println(aa.getAA().toJson());
        }
    }

}
