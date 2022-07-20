package com.pbalancer.client.controllers.cells;

import java.util.Arrays;
import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class PercentTableCell<T> extends TableCell<T,Double>
{
    private final String format;
    private final String positiveStyleClass;
    private final String negativeStyleClass;
    private final String zeroStyleClass;
    private final List<String> possibleStyleClasses;

    public PercentTableCell(
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

    public static class Factory<T> implements Callback<TableColumn<T,Double>, TableCell<T,Double>>
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
        public TableCell<T,Double> call(final TableColumn<T,Double> col)
        {
            return new PercentTableCell<T>(
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