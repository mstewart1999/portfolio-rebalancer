package com.msfinance.pbalancer.model;


public record PortfolioAlert(PortfolioAlert.Type type, PortfolioAlert.Level level, String text)
{
    public enum Type { AA, PORTFOLIO, ACCOUNT, ASSET, ACM, }
    public enum Level { INFO, WARN, ERROR }

}
