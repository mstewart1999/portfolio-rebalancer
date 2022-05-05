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
import com.msfinance.pbalancer.model.aa.AssetAllocation;
import com.msfinance.pbalancer.util.Validation;

public class Portfolio implements IPersistable
{
    private final String profileId;
    private final String id;
    private int listPosition;
    private String name;
    private PortfolioGoal goal;
    private BigDecimal lastValue;
    private Date lastValueTmstp;

    private Profile profile;
    private List<Account> accounts;
    private AssetAllocation targetAA;

    private boolean dirty = false;


    public Portfolio(
            final String profileId
            )
    {
        this(profileId, UUID.randomUUID().toString());
    }

    @JsonCreator
    public Portfolio(
            @JsonProperty("profileId") final String profileId,
            @JsonProperty("id") final String id
            )
    {
        this.profileId = Objects.requireNonNull(profileId);
        this.id = Objects.requireNonNull(id);
        listPosition = 0;
        name = "";
        goal = PortfolioGoal.Other;
        lastValue = null;
        lastValueTmstp = null;
        profile = null;
        accounts = new ArrayList<>();
        targetAA = new AssetAllocation();
    }

    @JsonProperty
    public String getProfileId()
    {
        return profileId;
    }

    @JsonProperty
    public String getId()
    {
        return id;
    }

    @JsonProperty
    public int getListPosition()
    {
        return listPosition;
    }

    @JsonProperty
    public String getName()
    {
        return name;
    }

    @JsonProperty
    public PortfolioGoal getGoal()
    {
        return goal;
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
    public Profile getProfile()
    {
        return profile;
    }

    @JsonIgnore
    public List<Account> getAccounts()
    {
        return accounts;
    }

    @JsonProperty
    public AssetAllocation getTargetAA()
    {
        return targetAA;
    }

    @JsonProperty
    public void setListPosition(final int listPosition)
    {
        this.listPosition = listPosition;
    }

    @JsonProperty
    public void setName(final String name)
    {
        this.name = Objects.requireNonNull(name);
    }

    @JsonProperty
    public void setGoal(final PortfolioGoal goal)
    {
        this.goal = Objects.requireNonNull(goal);
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
    public void setProfile(final Profile profile)
    {
        Validation.assertTrue(this.profileId.equals(profile.getId()));
        this.profile = profile;
    }

    @JsonIgnore
    public void setAccounts(final List<Account> accounts)
    {
        this.accounts = Objects.requireNonNull(accounts);
    }

    @JsonIgnore
    public void setTargetAA(final AssetAllocation targetAA)
    {
        this.targetAA = Objects.requireNonNull(targetAA);
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
