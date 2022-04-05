package com.msfinance.pbalancer.model.aa;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.msfinance.pbalancer.model.aa.PresetAA;

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
