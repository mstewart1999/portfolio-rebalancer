package com.pbalancer.client.controllers.cells;

import com.pbalancer.client.model.PortfolioGoal;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class PortfolioGoalListCell extends ListCell<PortfolioGoal>
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

    public static class Factory implements Callback<ListView<PortfolioGoal>, ListCell<PortfolioGoal>>
    {
        @Override
        public ListCell<PortfolioGoal> call(final ListView<PortfolioGoal> l)
        {
            return new PortfolioGoalListCell();
        }
    }
}