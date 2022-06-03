package com.msfinance.pbalancer.controllers;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

import org.controlsfx.control.textfield.AutoCompletionBinding.ISuggestionRequest;

import com.msfinance.pbalancer.model.aa.AssetTicker;

import impl.org.controlsfx.autocompletion.SuggestionProvider;

public class TickerSuggestionProvider extends SuggestionProvider<String>
{
    private boolean tickerOnlyMode;
    private final Map<String,AssetTicker> tickerSuggestions;

    public TickerSuggestionProvider(final Map<String,AssetTicker> tickerSuggestions)
    {
        this.tickerSuggestions = tickerSuggestions;
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
        if(tickerOnlyMode)
        {
            // case insensitive prefix matching (symbol only)
            String suggestionUpper = suggestion.toUpperCase();
            String userTextUpper = request.getUserText().toUpperCase();
            return suggestionUpper.startsWith(userTextUpper);
        }
        else
        {
            // case insensitive substring matching (security name not symbol)
            // poor man's lucene
            String suggestionUpper = tickerSuggestions.get(suggestion).getName().toUpperCase();
            String[] userTextUpper = request.getUserText().toUpperCase().split("\\s");
            int termCount = userTextUpper.length;
            int matchCount = 0;
            for(String term : userTextUpper)
            {
                if(suggestionUpper.contains(term))
                {
                    matchCount++;
                }
            }
            return termCount == matchCount;
        }
    }


    @Override
    public Collection<String> call(final ISuggestionRequest request)
    {
        tickerOnlyMode = true;
        Collection<String> suggestions = super.call(request);
        if(suggestions.size() == 0)
        {
            tickerOnlyMode = false;
            suggestions = super.call(request);
        }
        return suggestions;
    }
}
