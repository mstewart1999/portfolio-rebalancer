package com.msfinance.rebalancer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Account
{
    private final String id;
    private String name;
    private String institution;
    private AccountType type;

    private List<Asset> assets;

    public Account()
    {
        this(UUID.randomUUID().toString());
    }

    @JsonCreator
    public Account(
            @JsonProperty("id") final String id
            )
    {
        this.id = Objects.requireNonNull(id);
        name = "";
        institution = "";
        type = AccountType.UNDEFINED;
        assets = new ArrayList<>();
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
    public void setAssets(final List<Asset> assets)
    {
        this.assets = Objects.requireNonNull(assets);
    }

}
