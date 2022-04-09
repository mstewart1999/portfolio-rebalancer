package com.msfinance.pbalancer.model.aa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.msfinance.pbalancer.model.InvalidDataException;

public class TestAssetAllocation
{

    @Test
    public void aa1() throws Exception
    {
        AssetAllocation aa = aa(
            ",ROOT,All,1,1.0,R",
            "ROOT,EQ,Stocks,1,0.6,G",
            "ROOT,FI,Bonds,2,0.4,G",
            "EQ,EQ-US-TSM,EQ-US-TSM,1,70%,AC",
            "EQ,EQ-IG-TSM,EQ-IG-TSM,2,30%,AC",
            "FI,FI-IT-TBM,FI-IT-TBM,1,=100/10/10,AC"
        );
        System.out.println(aa.toCsv());
    }


    @Test
    public void tErrorBadCsv1() throws Exception
    {
        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            aa(
                ",ROOT,All,1,1.0,R,Extra,"
            );
        });

        Assertions.assertTrue(e.getMessage().contains("Wrong number of csv fields: 7"));
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
                ",X,All1,1,1.0,R",
                ",Y,All2,1,1.0,R"
            );
        });

        Assertions.assertTrue(e.getMessage().contains("Multiple roots found"));
    }

    @Test
    public void tErrorNoRoots() throws Exception
    {
        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            aa(
                "Y,X,All1,1,1.0,G",
                "X,Y,All2,1,1.0,G"
            );
        });

        Assertions.assertTrue(e.getMessage().contains("No root found"));
    }


    @Test
    public void tErrorDuplicateId() throws Exception
    {
        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            aa(
                ",ROOT,All,1,1.0,R",
                "ROOT,W,W1,1,0.5,G",
                "ROOT,W,W2,2,0.5,G"
            );
        });

        Assertions.assertTrue(e.getMessage().contains("Duplicate id found"));
    }



    @Test
    public void tErrorParentId() throws Exception
    {
        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            new AANode("x,z", "ID", "NM", 0, new DoubleExpression("1"), AANodeType.R).validate();
        });

        Assertions.assertTrue(e.getMessage().contains("invalid parentId"));
    }

    @Test
    public void tErrorId() throws Exception
    {
        // empty
        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            new AANode("ROOT", "", "NM", 0, new DoubleExpression("1"), AANodeType.R).validate();
        });

        Assertions.assertTrue(e.getMessage().contains("invalid id"));

        // comma
        InvalidDataException e2 = Assertions.assertThrows(InvalidDataException.class, () -> {
            new AANode("ROOT", "x,y", "NM", 0, new DoubleExpression("1"), AANodeType.R).validate();
        });

        Assertions.assertTrue(e2.getMessage().contains("invalid id"));
    }

    @Test
    public void tErrorName() throws Exception
    {
        // empty
        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            new AANode("ROOT", "ID", "", 0, new DoubleExpression("1"), AANodeType.R).validate();
        });

        Assertions.assertTrue(e.getMessage().contains("invalid name"));

        // comma
        InvalidDataException e2 = Assertions.assertThrows(InvalidDataException.class, () -> {
            new AANode("ROOT", "ID", "x,y", 0, new DoubleExpression("1"), AANodeType.R).validate();
        });

        Assertions.assertTrue(e2.getMessage().contains("invalid name"));
    }

    @Test
    public void tErrorPercentOfParent() throws Exception
    {
        // low
        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            new AANode("ROOT", "ID", "NM", 0, new DoubleExpression("-1"), AANodeType.R).validate();
        });

        Assertions.assertTrue(e.getMessage().contains("percentOfParent too low"));

        // high
        InvalidDataException e2 = Assertions.assertThrows(InvalidDataException.class, () -> {
            new AANode("ROOT", "ID", "NM", 0, new DoubleExpression("1.1"), AANodeType.R).validate();
        });

        Assertions.assertTrue(e2.getMessage().contains("percentOfParent too high"));
    }


    @Test
    public void tErrorRootId() throws Exception
    {
        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            aa(
                ",X,All,1,1.0,R",
                "X,EQ-US-TSM,EQ-US-TSM,1,1.0,AC"
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
                ",ROOT,All,1,0.80,R",
                "ROOT,EQ-US-TSM,EQ-US-TSM,1,1.0,AC"
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
                ",ROOT,All,1,1.0,R",
                "ROOT,EQ-US-TSM,EQ-US-ZZZ,1,1.0,AC"
            );
        });

        Assertions.assertTrue(e.getMessage().contains("name is not in AssetClass"));
    }

    @Test
    public void tErrorDuplicateChildPosition() throws Exception
    {
        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            aa(
                ",ROOT,All,1,1.0,R",
                "ROOT,EQ1,EQ-US-LV,1,0.50,AC",
                "ROOT,EQ2,EQ-US-LG,1,0.50,AC"
            );
        });

        Assertions.assertTrue(e.getMessage().contains("duplicate child.childPosition"));
    }

    @Test
    public void tErrorDuplicateChildName() throws Exception
    {
        InvalidDataException e = Assertions.assertThrows(InvalidDataException.class, () -> {
            aa(
                ",ROOT,All,1,1.0,R",
                "ROOT,EQ1,EQ-US-LV,1,0.50,AC",
                "ROOT,EQ2,EQ-US-LV,2,0.50,AC"
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
                ",ROOT,All,1,1.0,R",
                "ROOT,EQ1,EQ-US-LV,1,0.49,AC",
                "ROOT,EQ2,EQ-US-LG,2,0.50,AC"
            );
        });

        Assertions.assertTrue(e.getMessage().contains("sum(percentOfParent)"));

        // high
        InvalidDataException e2 = Assertions.assertThrows(InvalidDataException.class, () -> {
            aa(
                ",ROOT,All,1,1.0,R",
                "ROOT,EQ1,EQ-US-LV,1,0.51,AC",
                "ROOT,EQ2,EQ-US-LG,2,0.50,AC"
            );
        });

        Assertions.assertTrue(e2.getMessage().contains("sum(percentOfParent)"));
    }

    @Test
    public void tErrorParentChildMismatch() throws Exception
    {
        AANode r = new AANode("", "ROOT", "NM0", 0, new DoubleExpression("1"), AANodeType.R);
        AANode p = new AANode("ROOT", "X1", "NM1", 1, new DoubleExpression("0.5"), AANodeType.G);
        AANode p2 = new AANode("ROOT", "X2", "NM2", 2, new DoubleExpression("0.5"), AANodeType.G);
        AANode n = new AANode("X1", "EQ", "EQ-US-LV", 0, new DoubleExpression("1"), AANodeType.AC);
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
        return new AssetAllocation(null, lines);
    }

}
