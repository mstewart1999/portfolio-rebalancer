package com.pbalancer.client.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pbalancer.client.util.DateHelper;

public class Profile implements IPersistable
{
    public static final String DEFAULT = "DEFAULT";
    public static final String SAMPLE = "SAMPLE";

    private final String id;
    private String name;
    private BigDecimal lastValue;
    private Date lastValueTmstp;
    private Date lastValueTmstpLow;

    private boolean dirty = false;

    private List<Portfolio> portfolios;
    private ProfileSettings settings;


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
        lastValueTmstpLow = null;
        portfolios = new ArrayList<>();
        settings = null;
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

    @JsonProperty
    public Date getLastValueTmstpLow()
    {
        return lastValueTmstpLow;
    }

    @JsonIgnore
    public String getLastValueTmstpRange()
    {
        return String.format("%s to %s",
                DateHelper.formatISOLocalDate(lastValueTmstpLow),
                DateHelper.formatISOLocalDate(lastValueTmstp));
    }

    @JsonIgnore
    public List<Portfolio> getPortfolios()
    {
        return portfolios;
    }

    @JsonIgnore
    public ProfileSettings getSettings()
    {
        return settings;
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

    @JsonProperty
    public void setLastValueTmstpLow(final Date lastValueTmstpLow)
    {
        this.lastValueTmstpLow = lastValueTmstpLow;
    }

    @JsonIgnore
    public void setPortfolios(final List<Portfolio> portfolios)
    {
        this.portfolios = portfolios;
    }

    @JsonIgnore
    public void setSettings(final ProfileSettings settings)
    {
        this.settings = settings;
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

