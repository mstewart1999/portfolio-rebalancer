package com.msfinance.pbalancer.controllers.cells;

import java.util.Map;

import com.msfinance.pbalancer.model.aa.AssetTicker;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public final class AssetTickerListCell extends ListCell<String>
{
    private final Map<String, AssetTicker> tickerSuggestions;

    private AssetTickerListCell(final Map<String, AssetTicker> tickerSuggestions)
    {
        this.tickerSuggestions = tickerSuggestions;
    }

    @Override
    public void updateItem(final String item, final boolean empty)
    {
        super.updateItem(item, empty);
        if (empty) {
                setText(null);
        } else {
                setText(item + " -- " + tickerSuggestions.get(item).getName());
        }
    }

    public static class Factory implements Callback<ListView<String>, ListCell<String>>
    {
        private final Map<String, AssetTicker> tickerSuggestions;

        public Factory(final Map<String, AssetTicker> tickerSuggestions)
        {
            this.tickerSuggestions = tickerSuggestions;
        }

        @Override
        public ListCell<String> call(final ListView<String> l)
        {
            return new AssetTickerListCell(tickerSuggestions);
        }
    }
}