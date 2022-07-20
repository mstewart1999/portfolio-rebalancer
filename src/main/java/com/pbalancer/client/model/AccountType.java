package com.pbalancer.client.model;

public enum AccountType
{
    T_IRA("IRA", AccountTaxType.TAX_DEFERRED_TRAD),
    T_401k("401k", AccountTaxType.TAX_DEFERRED_TRAD),
    T_403b("403b", AccountTaxType.TAX_DEFERRED_TRAD),
    T_457b("457b", AccountTaxType.TAX_DEFERRED_TRAD),
    T_TSP("TSP", AccountTaxType.TAX_DEFERRED_TRAD),

    R_IRA("Roth IRA", AccountTaxType.TAX_DEFERRED_ROTH),
    R_401k("Roth 401k", AccountTaxType.TAX_DEFERRED_ROTH),
    R_403b("Roth 403b", AccountTaxType.TAX_DEFERRED_ROTH),
    R_457b("Roth 457b", AccountTaxType.TAX_DEFERRED_ROTH),
    R_TSP("Roth TSP", AccountTaxType.TAX_DEFERRED_ROTH),

    BROKERAGE("Taxable Brokerage", AccountTaxType.TAXABLE),
    MUTUTAL_FUND("Taxable Mutual Fund(s)", AccountTaxType.TAXABLE),
    BANK("Bank Savings/MM/Checking/CD", AccountTaxType.TAXABLE),
    ANNUITY("Variable Annuity", AccountTaxType.TAX_DEFERRED_INSURANCE),

    E_529("529", AccountTaxType.TAX_DEFERRED_EDUCATION),
    E_COVERDELL("Coverdell ESA", AccountTaxType.TAX_DEFERRED_EDUCATION),

    HSA("HSA", AccountTaxType.TAX_DEFERRED_MEDICAL),

    OTHER_TD("Other tax deferred", AccountTaxType.TAX_DEFERRED_TRAD),
    OTHER_TAXABLE("Other taxable", AccountTaxType.TAXABLE),

    UNDEFINED("Undefined", AccountTaxType.UNDEFINED),
    ;

    private String text;
    private AccountTaxType type;

    AccountType(final String text, final AccountTaxType type)
    {
        this.text = text;
        this.type = type;
    }

    public String getText()
    {
        return text;
    }

    public AccountTaxType getType()
    {
        return type;
    }
}
