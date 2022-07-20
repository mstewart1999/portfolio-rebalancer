package com.pbalancer.client.model.aa;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.pbalancer.client.model.aa.PredefinedAA;

class TestPredefinedAA
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
