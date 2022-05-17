package com.msfinance.pbalancer.controllers.cells;

import javafx.geometry.Pos;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

public class PercentTreeTableCell<T> extends TreeTableCell<T,Double>
{
    private final String format;

    public PercentTreeTableCell(final String format)
    {
        this.format = format;
    }

    @Override
    protected void updateItem(final Double value, final boolean empty)
    {
        super.updateItem(value, empty);
        if(empty || (value == null))
        {
            setText("");
        }
        else
        {
            setText(String.format(format, value.doubleValue()*100) + " %");
            setAlignment(Pos.TOP_RIGHT); // CENTER_RIGHT seems more appropriate, but doesn't align with other cells
        }
    }

    public static class Factory<T> implements Callback<TreeTableColumn<T,Double>, TreeTableCell<T,Double>>
    {
        private String format;

        public Factory()
        {
            this("%6.2f");
        }

        public Factory(final String format)
        {
            this.format = format;
        }

        @Override
        public TreeTableCell<T,Double> call(final TreeTableColumn<T,Double> col)
        {
            return new PercentTreeTableCell<T>(format);
        }
    }
}