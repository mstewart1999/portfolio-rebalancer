package com.msfinance.pbalancer.controllers.cells;

import com.msfinance.pbalancer.model.PortfolioGoal;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class PortfolioGoalTableCell<T> extends TableCell<T,PortfolioGoal>
{

    @Override
    protected void updateItem(final PortfolioGoal value, final boolean empty)
    {
        if (value == getItem() && empty == isEmpty()) return;

        super.updateItem(value, empty);
        if(value == null)
        {
            setText("");
        }
        else
        {
            setText(value.getText());
        }
    }

    public static class Factory<T> implements Callback<TableColumn<T,PortfolioGoal>, TableCell<T,PortfolioGoal>>
    {
        @Override
        public TableCell<T,PortfolioGoal> call(final TableColumn<T,PortfolioGoal> col)
        {
            return new PortfolioGoalTableCell<T>();
        }
    }
}