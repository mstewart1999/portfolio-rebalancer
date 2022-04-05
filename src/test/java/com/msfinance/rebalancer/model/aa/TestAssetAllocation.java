package com.msfinance.rebalancer.model.aa;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.msfinance.rebalancer.model.InvalidDataException;

public class TestAssetAllocation
{

    @Test
    public void aa1() throws Exception
    {
        AssetAllocation aa = aa(
            ",ROOT,All,1,1.0",
            "ROOT,EQ,Stocks,1,0.6",
            "ROOT,FI,Bonds,2,0.4",
            "EQ,EQ-US-TSM,EQ-US-TSM,1,70%",
            "EQ,EQ-IG-TSM,EQ-IG-TSM,2,30%",
            "FI,FI-IT-TBM,FI-IT-TBM,1,=100/10/10"
        );
        System.out.println(aa.toCsv());
    }


    @Test
    public void tErrorBadCsv1() throws Exception
    {
        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            aa(
                ",ROOT,All,1,1.0,Extra"
            );
        });

        Assertions.assertTrue(e.getMessage().contains("Wrong number of csv fields: 6"));
    }

    @Test
    public void tErrorBadCsv2() throws Exception
    {
        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            aa(
                ",ROOT,All,1"
            );
        });

        Assertions.assertTrue(e.getMessage().contains("Wrong number of csv fields: 4"));
    }

    @Test
    public void tErrorMultipleRoots() throws Exception
    {
        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            aa(
                ",X,All1,1,1.0",
                ",Y,All2,1,1.0"
            );
        });

        Assertions.assertTrue(e.getMessage().contains("Multiple roots found"));
    }

    @Test
    public void tErrorNoRoots() throws Exception
    {
        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            aa(
                "Y,X,All1,1,1.0",
                "X,Y,All2,1,1.0"
            );
        });

        Assertions.assertTrue(e.getMessage().contains("No root found"));
    }


    @Test
    public void tErrorDuplicateId() throws Exception
    {
        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            aa(
                ",ROOT,All,1,1.0",
                "ROOT,W,W1,1,0.5",
                "ROOT,W,W2,2,0.5"
            );
        });

        Assertions.assertTrue(e.getMessage().contains("Duplicate id found"));
    }



    @Test
    public void tErrorParentId() throws Exception
    {
        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            new AANode("x,z", "ID", "NM", 0, new DoubleExpression("1")).validate();
        });

        Assertions.assertTrue(e.getMessage().contains("invalid parentId"));
    }

    @Test
    public void tErrorId() throws Exception
    {
        // empty
        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            new AANode("ROOT", "", "NM", 0, new DoubleExpression("1")).validate();
        });

        Assertions.assertTrue(e.getMessage().contains("invalid id"));

        // comma
        InvalidDataException e2 = Assertions.assertThrows(InvalidDataException.class, () -> {
            new AANode("ROOT", "x,y", "NM", 0, new DoubleExpression("1")).validate();
        });

        Assertions.assertTrue(e2.getMessage().contains("invalid id"));
    }

    @Test
    public void tErrorName() throws Exception
    {
        // empty
        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            new AANode("ROOT", "ID", "", 0, new DoubleExpression("1")).validate();
        });

        Assertions.assertTrue(e.getMessage().contains("invalid name"));

        // comma
        InvalidDataException e2 = Assertions.assertThrows(InvalidDataException.class, () -> {
            new AANode("ROOT", "ID", "x,y", 0, new DoubleExpression("1")).validate();
        });

        Assertions.assertTrue(e2.getMessage().contains("invalid name"));
    }

    @Test
    public void tErrorPercentOfParent() throws Exception
    {
        // low
        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            new AANode("ROOT", "ID", "NM", 0, new DoubleExpression("-1")).validate();
        });

        Assertions.assertTrue(e.getMessage().contains("percentOfParent too low"));

        // high
        InvalidDataException e2 = Assertions.assertThrows(InvalidDataException.class, () -> {
            new AANode("ROOT", "ID", "NM", 0, new DoubleExpression("1.1")).validate();
        });

        Assertions.assertTrue(e2.getMessage().contains("percentOfParent too high"));
    }


    @Test
    public void tErrorRootId() throws Exception
    {
        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            aa(
                ",X,All,1,1.0",
                "X,EQ-US-TSM,EQ-US-TSM,1,1.0"
            );
        });

        Assertions.assertTrue(e.getMessage().contains("Root"));
        Assertions.assertTrue(e.getMessage().contains("id=ROOT"));
    }

    @Test
    public void tErrorRootPercentOfParent() throws Exception
    {
        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            aa(
                ",ROOT,All,1,0.80",
                "ROOT,EQ-US-TSM,EQ-US-TSM,1,1.0"
            );
        });

        Assertions.assertTrue(e.getMessage().contains("Root"));
        Assertions.assertTrue(e.getMessage().contains("percentOfParent"));
    }

    @Test
    public void tErrorLeafNotAssetClass() throws Exception
    {
        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            aa(
                ",ROOT,All,1,1.0",
                "ROOT,EQ-US-TSM,EQ-US-ZZZ,1,1.0"
            );
        });

        Assertions.assertTrue(e.getMessage().contains("name is not in AssetClass"));
    }

    @Test
    public void tErrorDuplicateChildPosition() throws Exception
    {
        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            aa(
                ",ROOT,All,1,1.0",
                "ROOT,EQ1,EQ-US-LV,1,0.50",
                "ROOT,EQ2,EQ-US-LG,1,0.50"
            );
        });

        Assertions.assertTrue(e.getMessage().contains("duplicate child.childPosition"));
    }

    @Test
    public void tErrorDuplicateChildName() throws Exception
    {
        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            aa(
                ",ROOT,All,1,1.0",
                "ROOT,EQ1,EQ-US-LV,1,0.50",
                "ROOT,EQ2,EQ-US-LV,2,0.50"
            );
        });

        Assertions.assertTrue(e.getMessage().contains("duplicate child.name"));
    }

    @Test
    public void tErrorInvalidChildSum() throws Exception
    {
        // low
        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            aa(
                ",ROOT,All,1,1.0",
                "ROOT,EQ1,EQ-US-LV,1,0.49",
                "ROOT,EQ2,EQ-US-LG,2,0.50"
            );
        });

        Assertions.assertTrue(e.getMessage().contains("sum(percentOfParent)"));

        // high
        InvalidDataException e2 = Assertions.assertThrows(InvalidDataException.class, () -> {
            aa(
                ",ROOT,All,1,1.0",
                "ROOT,EQ1,EQ-US-LV,1,0.51",
                "ROOT,EQ2,EQ-US-LG,2,0.50"
            );
        });

        Assertions.assertTrue(e2.getMessage().contains("sum(percentOfParent)"));
    }

    @Test
    public void tErrorParentChildMismatch() throws Exception
    {
        AANode r = new AANode("", "ROOT", "NM0", 0, new DoubleExpression("1"));
        AANode p = new AANode("ROOT", "X1", "NM1", 1, new DoubleExpression("0.5"));
        AANode p2 = new AANode("ROOT", "X2", "NM2", 2, new DoubleExpression("0.5"));
        AANode n = new AANode("X1", "EQ", "EQ-US-LV", 0, new DoubleExpression("1"));
        r.addChild(p);
        r.addChild(p2);
        p.setParent(r);
        p2.setParent(r);
        p.addChild(n);
        n.setParent(p2); // error here

        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            r.validate();
        });

        Assertions.assertTrue(e.getMessage().contains("parent/child mismatch"));
    }


    private static AssetAllocation aa(final String... lines) throws InvalidDataException
    {
        return new AssetAllocation( List.of(lines) );
    }

}
