package com.msfinance.pbalancer.model;

public enum AccountTaxType
{
    TAX_DEFERRED_TRAD(true),
    TAX_DEFERRED_ROTH(true),
    TAX_DEFERRED_EDUCATION(true),
    TAX_DEFERRED_MEDICAL(true),
    TAX_DEFERRED_INSURANCE(true),
    TAXABLE(false),
    UNDEFINED(false),
    ;

    private boolean taxAdvantaged;

    private AccountTaxType(final boolean taxAdvantaged)
    {
        this.taxAdvantaged = taxAdvantaged;
    }

    public boolean isTaxAdvantaged()
    {
        return taxAdvantaged;
    }
}
