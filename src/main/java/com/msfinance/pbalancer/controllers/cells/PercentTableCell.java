package com.msfinance.pbalancer.controllers.cells;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class PercentTableCell<T> extends TableCell<T,Double>
{
    private final String format;

    public PercentTableCell(final String format)
    {
        this.format = format;
    }

    @Override
    protected void updateItem(final Double value, final boolean empty)
    {
        if (value == getItem() && empty == isEmpty()) return;

        super.updateItem(value, empty);
        if(value == null)
        {
            setText("");
        }
        else
        {
            setText(String.format(format, value.doubleValue()*100) + " %");
            setAlignment(Pos.CENTER_RIGHT);
        }
    }

    public static class Factory<T> implements Callback<TableColumn<T,Double>, TableCell<T,Double>>
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
        public TableCell<T,Double> call(final TableColumn<T,Double> col)
        {
            return new PercentTableCell<T>(format);
        }
    }
}