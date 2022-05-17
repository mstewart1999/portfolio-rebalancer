package com.msfinance.pbalancer.controllers.cells;

import java.util.Arrays;
import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

public class PercentTreeTableCell<T> extends TreeTableCell<T,Double>
{
    private final String format;
    private final String positiveStyleClass;
    private final String negativeStyleClass;
    private final String zeroStyleClass;
    private final List<String> possibleStyleClasses;

    public PercentTreeTableCell(
            final String format,
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
    protected void updateItem(final Double value, final boolean empty)
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
            setText(String.format(format, value.doubleValue()*100) + " %");
            setAlignment(Pos.TOP_RIGHT); // CENTER_RIGHT seems more appropriate, but doesn't align with other cells
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

    public static class Factory<T> implements Callback<TreeTableColumn<T,Double>, TreeTableCell<T,Double>>
    {
        private String format;
        private final String positiveStyleClass;
        private final String negativeStyleClass;
        private final String zeroStyleClass;

        public Factory()
        {
            this("%6.2f", null, null, null);
        }

        public Factory(
                final String format,
                final String positiveStyleClass,
                final String negativeStyleClass,
                final String zeroStyleClass
                )
        {
            this.format = format;
            this.positiveStyleClass = positiveStyleClass;
            this.negativeStyleClass = negativeStyleClass;
            this.zeroStyleClass = zeroStyleClass;
        }

        @Override
        public TreeTableCell<T,Double> call(final TreeTableColumn<T,Double> col)
        {
            return new PercentTreeTableCell<T>(
                    format,
                    positiveStyleClass,
                    negativeStyleClass,
                    zeroStyleClass);
        }
    }

    public static class ColoredFactory<T> extends Factory<T>
    {
        public ColoredFactory()
        {
            super(
                    "%6.2f",
                    "pb-positive-number",
                    "pb-negative-number",
                    "pb-zero-number"
                    );
        }
    }

}