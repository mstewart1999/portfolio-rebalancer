package com.pbalancer.client.model.aa;

import java.util.Objects;

import com.pbalancer.client.model.InvalidDataException;

import net.objecthunter.exp4j.ExpressionBuilder;

public class DoubleExpression
{
    private final String expr;
    private double value;

    public static DoubleExpression createSafe0Percent()
    {
        return new DoubleExpression("0%", 0.0);
    }
    public static DoubleExpression createSafe100Percent()
    {
        return new DoubleExpression("100%", 1.0);
    }


    public DoubleExpression(final String expr) throws InvalidDataException
    {
        this.expr = Objects.requireNonNullElse(expr, "").trim();
        try
        {
            if(this.expr.isEmpty())
            {
                value = 0.0;
            }
            else if(this.expr.startsWith("="))
            {
                // exp4j: https://www.baeldung.com/java-evaluate-math-expression-string
                String tmp = this.expr.substring(1);
                value = new ExpressionBuilder(tmp).build().evaluate();
            }
            else if(this.expr.endsWith("%"))
            {
                String tmp = this.expr.substring(0,this.expr.length()-1);
                value = Double.valueOf(tmp) / 100.0;
            }
            else
            {
                value = Double.valueOf(expr);
            }
        }
        catch (Exception e)
        {
            throw new InvalidDataException("Bad DoubleExpression '" + expr + "'", e);
        }
    }

    private DoubleExpression(final String expr, final double value)
    {
        this.expr = expr;
        this.value = value;
    }

    public String getExpr()
    {
        return expr;
    }

    // TODO: remove from json
    public double getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return "DoubleExpression [expr=" + expr + ", value=" + value + "]";
    }
}
