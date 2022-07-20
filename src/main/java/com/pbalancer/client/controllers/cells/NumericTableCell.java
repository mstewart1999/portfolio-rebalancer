package com.pbalancer.client.controllers.cells;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class NumericTableCell<T> extends TableCell<T,Number>
{
    private final DecimalFormat format;
    private final String positiveStyleClass;
    private final String negativeStyleClass;
    private final String zeroStyleClass;
    private final List<String> possibleStyleClasses;

    public NumericTableCell(
            final DecimalFormat format,
            final String positiveStyleClass,
            final String negativeStyleClass,
            final String zeroStyleClass
            )
    {
        this.format = format;
        this.positiveStyleClass = positiveStyleClass;
        this.negativeStyleClass = negativeStyleClass;
        this.zeroStyleClass = zeroStyleClass;
        this.possibleStyleClasses = Arrays.asList(positiveStyleClass, negativeStyleClass, zeroStyleClass);
    }

    @Override
    protected void updateItem(final Number value, final boolean empty)
    {
        super.updateItem(value, empty);

        // reset colors
        this.getStyleClass().removeAll(possibleStyleClasses);

        if(empty || (value == null))
        {
            setText("");
        }
        else
        {
            setText(format.format(value));
            setAlignment(Pos.TOP_RIGHT);
            if(value.doubleValue() > 0)
            {
                if(positiveStyleClass != null)
                {
                    this.getStyleClass().add(positiveStyleClass);
                }
            }
            else if(value.doubleValue() < 0)
            {
                if(negativeStyleClass != null)
                {
                    this.getStyleClass().add(negativeStyleClass);
                }
            }
            else
            {
                if(zeroStyleClass != null)
                {
                    this.getStyleClass().add(zeroStyleClass);
                }
            }
        }
    }

    private static class Factory<T> implements Callback<TableColumn<T,Number>, TableCell<T,Number>>
    {
        private final DecimalFormat format;
        private final String positiveStyleClass;
        private final String negativeStyleClass;
        private final String zeroStyleClass;

        public Factory(final String dFormatStr)
        {
            this(dFormatStr, null, null, null);
        }

        public Factory(
                final String dFormatStr,
                final String positiveStyleClass,
                final String negativeStyleClass,
                final String zeroStyleClass
                )
        {
            this.format = new DecimalFormat(dFormatStr);
            this.positiveStyleClass = positiveStyleClass;
            this.negativeStyleClass = negativeStyleClass;
            this.zeroStyleClass = zeroStyleClass;
        }

        @Override
        public TableCell<T,Number> call(final TableColumn<T,Number> col)
        {
            return new NumericTableCell<T>(
                    format,
                    positiveStyleClass,
                    negativeStyleClass,
                    zeroStyleClass);
        }
    }

    public static class CurrencyFactory<T> extends Factory<T>
    {
        public CurrencyFactory()
        {
            super("#,##0.00");
        }
    }

    public static class ColoredCurrencyFactory<T> extends Factory<T>
    {
        public ColoredCurrencyFactory()
        {
            super(
                    "#,##0.00",
                    "pb-positive-number",
                    "pb-negative-number",
                    "pb-zero-number"
                    );

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