package com.pbalancer.client.controllers.cells;

import com.pbalancer.client.model.aa.PredefinedAA;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class PredefinedAAListCell extends ListCell<PredefinedAA>
{
    @Override
    protected void updateItem(final PredefinedAA p, final boolean empty)
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

    public static class Factory implements Callback<ListView<PredefinedAA>, ListCell<PredefinedAA>>
    {
        @Override
        public ListCell<PredefinedAA> call(final ListView<PredefinedAA> l)
        {
            return new PredefinedAAListCell();
        }
    }
}