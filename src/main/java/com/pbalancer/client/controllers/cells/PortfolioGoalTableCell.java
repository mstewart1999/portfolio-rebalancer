package com.pbalancer.client.controllers.cells;

import com.pbalancer.client.model.PortfolioGoal;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class PortfolioGoalTableCell<T> extends TableCell<T,PortfolioGoal>
{

    @Override
    protected void updateItem(final PortfolioGoal e, final boolean empty)
    {
        super.updateItem(e, empty);
        if(empty || (e == null))
        {
            setText("");
        }
        else
        {
            setText(e.getText());
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