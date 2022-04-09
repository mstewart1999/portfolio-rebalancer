package com.msfinance.pbalancer.controllers;

import com.msfinance.pbalancer.model.Portfolio;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

class PortfolioListCell extends ListCell<Portfolio>
{
    @Override
    protected void updateItem(final Portfolio p, final boolean empty)
    {
        super.updateItem(p, empty);
        if(p != null)
        {
            setText(p.getName() + "  [" + p.getGoal().name() + "]");
        }
        else
        {
            setText("");
        }
    }

    public static class Factory implements Callback<ListView<Portfolio>, ListCell<Portfolio>>
    {
        @Override
        public ListCell<Portfolio> call(final ListView<Portfolio> l)
        {
            return new PortfolioListCell();
        }
    }
}