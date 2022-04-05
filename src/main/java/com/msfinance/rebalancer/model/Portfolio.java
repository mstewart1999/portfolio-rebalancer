package com.msfinance.rebalancer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.msfinance.rebalancer.model.aa.AssetAllocation;

public class Portfolio
{
    private final String id;
    private String name;
    private PortfolioGoal goal;

    private List<Account> accounts;
    private AssetAllocation targetAA;

    public Portfolio()
    {
        this(UUID.randomUUID().toString());
    }

    @JsonCreator
    public Portfolio(
            @JsonProperty("id") final String id
            )
    {
        this.id = Objects.requireNonNull(id);
        name = "";
        goal = PortfolioGoal.Other;
        accounts = new ArrayList<>();
        targetAA = new AssetAllocation();
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
    public PortfolioGoal getGoal()
    {
        return goal;
    }

    @JsonProperty
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
    public void setAccounts(final List<Account> accounts)
    {
        this.accounts = Objects.requireNonNull(accounts);
    }

    @JsonProperty
    public void setTargetAA(final AssetAllocation targetAA)
    {
        this.targetAA = Objects.requireNonNull(targetAA);
    }

}
