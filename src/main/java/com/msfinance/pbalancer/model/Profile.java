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

public class Profile implements IPersistable
{
    public static final String DEFAULT = "DEFAULT";
    public static final String SAMPLE = "SAMPLE";

    private final String id;
    private String name;
    private BigDecimal lastValue;
    private Date lastValueTmstp;

    private boolean dirty = false;

    private List<Portfolio> portfolios;

    public Profile()
    {
        this(UUID.randomUUID().toString());
    }

    @JsonCreator
    public Profile(
            @JsonProperty("id") final String id
            )
    {
        this.id = Objects.requireNonNull(id);
        name = "";
        lastValue = null;
        lastValueTmstp = null;
        portfolios = new ArrayList<>();
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
    public List<Portfolio> getPortfolios()
    {
        return portfolios;
    }

    @JsonProperty
    public void setName(final String name)
    {
        this.name = Objects.requireNonNull(name);
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
    public void setPortfolios(final List<Portfolio> portfolios)
    {
        this.portfolios = portfolios;
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

