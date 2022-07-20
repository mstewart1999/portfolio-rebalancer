package com.pbalancer.client.model;

public enum PortfolioGoal
{
    Retirement("Retirement"),
    Education("Education"),
    Purchase("Purchase (home, vehicle, etc..)"),
    Emergency_Fund("Emergency Fund"),
    Bequest("Bequest"),
    Other("Other"),
    ;

    private String text;

    PortfolioGoal(final String text)
    {
        this.text = text;
    }

    public String getText()
    {
        return text;
    }
}
