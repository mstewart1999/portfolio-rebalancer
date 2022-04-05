package com.msfinance.pbalancer.model.aa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.msfinance.pbalancer.model.InvalidDataException;
import com.msfinance.pbalancer.model.aa.DoubleExpression;

class TestDoubleExpression
{
    private static final double EPS = 0.000001;

    @Test
    void test() throws InvalidDataException
    {
        // empty
        assertEquals(0.00, new DoubleExpression(null).getValue(), EPS);
        assertEquals(0.00, new DoubleExpression("").getValue(), EPS);
        assertEquals(0.00, new DoubleExpression("    ").getValue(), EPS);

        // number
        assertEquals(1.00, new DoubleExpression("1").getValue(), EPS);
        assertEquals(0.01, new DoubleExpression("1e-2").getValue(), EPS);

        // percent
        assertEquals(0.01, new DoubleExpression("1%").getValue(), EPS);

        // expression
        assertEquals(0.20, new DoubleExpression("=1/5").getValue(), EPS);
    }

    @Test
    void testBad() throws InvalidDataException
    {
        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            new DoubleExpression("5x");
        });

        Assertions.assertTrue(e.getMessage().contains("Bad DoubleExpression"));
    }

    @Test
    void testGetExpr() throws InvalidDataException
    {
        assertEquals("=1/3", new DoubleExpression("=1/3").getExpr());
    }

    @Test
    void testToString() throws InvalidDataException
    {
        new DoubleExpression("=1/3").toString();
    }
}
