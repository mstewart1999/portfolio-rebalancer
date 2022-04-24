package com.msfinance.pbalancer.controllers.cells;

import java.text.DecimalFormat;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class NumericTableCell<T> extends TableCell<T,Number>
{
    private final DecimalFormat format;

    public NumericTableCell(final DecimalFormat format)
    {
        this.format = format;
    }

    @Override
    protected void updateItem(final Number value, final boolean empty)
    {
        if (value == getItem() && empty == isEmpty()) return;

        super.updateItem(value, empty);
        if(value == null)
        {
            setText("");
            // reset background color
        }
        else
        {
            setText(format.format(value));
            setAlignment(Pos.CENTER_RIGHT);
            if(value.doubleValue() > 0)
            {
                // TODO: green background?  only for currency?
            }
            else if(value.doubleValue() < 0)
            {
                // TODO: red background?  only for currency?
            }
            else
            {
                // TODO: white background?  only for currency?
            }
        }
    }

    private static class Factory<T> implements Callback<TableColumn<T,Number>, TableCell<T,Number>>
    {
        private final DecimalFormat format;

        public Factory(final String dFormatStr)
        {
            this.format = new DecimalFormat(dFormatStr);
        }

        @Override
        public TableCell<T,Number> call(final TableColumn<T,Number> col)
        {
            return new NumericTableCell<T>(format);
        }
    }

    public static class CurrencyFactory<T> extends Factory<T>
    {
        public CurrencyFactory()
        {
            super("#,##0.00");
        }
    }

    public static class UnitsFactory<T> extends Factory<T>
    {
        public UnitsFactory()
        {
            super("#,##0.000");
        }
    }
}