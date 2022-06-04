package com.msfinance.pbalancer.controllers.cells;

import java.util.Map;

import com.msfinance.pbalancer.model.aa.AssetClass;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public final class AssetClassListCell extends ListCell<String>
{
    private final Map<String, AssetClass> suggestions;

    public AssetClassListCell(final Map<String, AssetClass> suggestions)
    {
        this.suggestions = suggestions;
    }

    @Override
    public void updateItem(final String item, final boolean empty)
    {
        super.updateItem(item, empty);
        if (empty || (item == null))
        {
            setText(null);
        }
        else
        {
            AssetClass ac = suggestions.get(item);
            if(ac == null)
            {
                setText(item);
            }
            else
            {
                setText(item + " -- " + ac.getShortDescription());
            }
        }
    }

    public static class Factory implements Callback<ListView<String>, ListCell<String>>
    {
        private final Map<String, AssetClass> suggestions;

        public Factory(final Map<String, AssetClass> suggestions)
        {
            this.suggestions = suggestions;
        }

        @Override
        public ListCell<String> call(final ListView<String> l)
        {
            return new AssetClassListCell(suggestions);
        }
    }
}