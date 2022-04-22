package com.msfinance.pbalancer.controllers;

import java.util.Comparator;
import java.util.Map;

import org.controlsfx.control.textfield.AutoCompletionBinding.ISuggestionRequest;

import com.msfinance.pbalancer.model.aa.AssetTicker;

import impl.org.controlsfx.autocompletion.SuggestionProvider;

public class TickerSuggestionProvider extends SuggestionProvider<String>
{
    public TickerSuggestionProvider(final Map<String,AssetTicker> tickerSuggestions)
    {
        addPossibleSuggestions(tickerSuggestions.keySet());
    }

    @Override
    protected Comparator<String> getComparator()
    {
        return Comparator.naturalOrder();
    }

    @Override
    protected boolean isMatch(final String suggestion, final ISuggestionRequest request)
    {
        // case insensitive prefix matching
        String suggestionUpper = suggestion.toUpperCase();
        String userTextUpper = request.getUserText().toUpperCase();
        return suggestionUpper.startsWith(userTextUpper);
    }

}
