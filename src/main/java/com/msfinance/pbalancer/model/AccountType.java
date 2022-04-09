package com.msfinance.pbalancer.model;

public enum AccountType
{
    TAX_DEFERRED_TRAD(""), // IRA, 401k, 457b, 403b, TSP
    TAX_DEFERRED_ROTH(""), // Roth IRA, Roth 401k, Roth 457
    TAXABLE(""),
    UNDEFINED(""),
    ;

    private String text;

    AccountType(final String text)
    {
        this.text = text;
    }

    public String getText()
    {
        return text;
    }
}
