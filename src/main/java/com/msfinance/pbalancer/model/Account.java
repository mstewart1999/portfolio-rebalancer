package com.msfinance.pbalancer.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.msfinance.pbalancer.util.Validation;

public class Account implements IPersistable
{
    private final String portfolioId;
    private final String id;
    private String name;
    private String institution;
    private AccountType type;
    private int listPosition;
    private BigDecimal lastValue;
    private Date lastValueTmstp;

    private Portfolio portfolio;
    private List<Asset> assets;

    private boolean dirty = false;


    public Account(
            final String portfolioId
            )
    {
        this(portfolioId, UUID.randomUUID().toString());
    }

    @JsonCreator
    public Account(
            @JsonProperty("portfolioId") final String portfolioId,
            @JsonProperty("id") final String id
            )
    {
        this.portfolioId = Objects.requireNonNull(portfolioId);
        this.id = Objects.requireNonNull(id);
        name = "";
        institution = "";
        type = AccountType.UNDEFINED;
        listPosition = 0;
        lastValue = null;
        lastValueTmstp = null;
        portfolio = null;
        assets = new ArrayList<>();
    }

    @JsonProperty
    public String getPortfolioId()
    {
        return portfolioId;
    }

    @JsonProperty
    public String getId()
    {
        return id;
    }

    @JsonProperty
    public String getName()
    {
        return name;
    }

    @JsonProperty
    public String getInstitution()
    {
        return institution;
    }

    @JsonProperty
    public AccountType getType()
    {
        return type;
    }

    @JsonProperty
    public int getListPosition()
    {
        return listPosition;
    }

    @JsonProperty
    public BigDecimal getLastValue()
    {
        return lastValue;
    }

    @JsonProperty
    public Date getLastValueTmstp()
    {
        return lastValueTmstp;
    }

    @JsonIgnore
    public Portfolio getPortfolio()
    {
        return portfolio;
    }

    @JsonIgnore
    public List<Asset> getAssets()
    {
        return assets;
    }

    @JsonProperty
    public void setName(final String name)
    {
        this.name = Objects.requireNonNull(name);
    }

    @JsonProperty
    public void setInstitution(final String institution)
    {
        this.institution = Objects.requireNonNull(institution);
    }

    @JsonProperty
    public void setType(final AccountType type)
    {
        this.type = Objects.requireNonNull(type);
    }

    @JsonProperty
    public void setListPosition(final int listPosition)
    {
        this.listPosition = listPosition;
    }

    @JsonProperty
    public void setLastValue(final BigDecimal lastValue)
    {
        this.lastValue = lastValue;
    }

    @JsonProperty
    public void setLastValueTmstp(final Date lastValueTmstp)
    {
        this.lastValueTmstp = lastValueTmstp;
    }

    @JsonIgnore
    public void setPortfolio(final Portfolio portfolio)
    {
        Validation.assertTrue(this.portfolioId.equals(portfolio.getId()));
        this.portfolio = portfolio;
    }

    @JsonIgnore
    public void setAssets(final List<Asset> assets)
    {
        this.assets = Objects.requireNonNull(assets);
    }


    @Override
    public void markDirty()
    {
        this.dirty = true;
    }

    @Override
    public void markClean()
    {
        this.dirty = false;
    }

    @Override
    @JsonIgnore
    public boolean isDirty()
    {
        return this.dirty;
    }
}
