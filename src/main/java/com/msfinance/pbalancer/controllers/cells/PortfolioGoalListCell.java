package com.msfinance.pbalancer.controllers.cells;

import com.msfinance.pbalancer.model.PortfolioGoal;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class PortfolioGoalListCell extends ListCell<PortfolioGoal>
{
    @Override
    protected void updateItem(final PortfolioGoal p, final boolean empty)
    {
        super.updateItem(p, empty);
        if(p != null)
        {
            setText(p.getText());
        }
        else
        {
            setText("");
        }
    }

    public static class Factory implements Callback<ListView<PortfolioGoal>, ListCell<PortfolioGoal>>
    {
        @Override
        public ListCell<PortfolioGoal> call(final ListView<PortfolioGoal> l)
        {
            return new PortfolioGoalListCell();
        }
    }
}