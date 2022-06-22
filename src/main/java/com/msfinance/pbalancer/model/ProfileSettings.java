package com.msfinance.pbalancer.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProfileSettings implements IPersistable
{
    private final String profileId;
    private final String id;
    private int rebalanceCheckIntervalDays;
    private double rebalanceToleranceBandAbsolute;
    private double rebalanceToleranceBandRelative;
    private BigDecimal rebalanceMinimumDollars;
    private int assetPricingAgeWarningDays;

    private boolean dirty = false;


    public ProfileSettings(
            final String profileId
            )
    {
        this(profileId, UUID.randomUUID().toString());
    }

    @JsonCreator
    public ProfileSettings(
            @JsonProperty("profileId") final String profileId,
            @JsonProperty("id") final String id
            )
    {
        this.profileId = Objects.requireNonNull(profileId);
        this.id = Objects.requireNonNull(id);
        this.rebalanceCheckIntervalDays = 1;
        this.rebalanceToleranceBandAbsolute = 0.05;
        this.rebalanceToleranceBandRelative = 0.25;
        this.rebalanceMinimumDollars = BigDecimal.ZERO;
        this.assetPricingAgeWarningDays = 90;
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
    public int getRebalanceCheckIntervalDays()
    {
        return rebalanceCheckIntervalDays;
    }

    @JsonProperty
    public double getRebalanceToleranceBandAbsolute()
    {
        return rebalanceToleranceBandAbsolute;
    }

    @JsonProperty
    public double getRebalanceToleranceBandRelative()
    {
        return rebalanceToleranceBandRelative;
    }

    @JsonProperty
    public BigDecimal getRebalanceMinimumDollars()
    {
        return rebalanceMinimumDollars;
    }

    @JsonProperty
    public int getAssetPricingAgeWarningDays()
    {
        return assetPricingAgeWarningDays;
    }


    @JsonProperty
    public void setRebalanceCheckIntervalDays(final int rebalanceCheckIntervalDays)
    {
        this.rebalanceCheckIntervalDays = rebalanceCheckIntervalDays;
    }

    @JsonProperty
    public void setRebalanceToleranceBandAbsolute(final double rebalanceToleranceBandAbsolute)
    {
        this.rebalanceToleranceBandAbsolute = rebalanceToleranceBandAbsolute;
    }

    @JsonProperty
    public void setRebalanceToleranceBandRelative(final double rebalanceToleranceBandRelative)
    {
        this.rebalanceToleranceBandRelative = rebalanceToleranceBandRelative;
    }

    @JsonProperty
    public void setRebalanceMinimumDollars(final BigDecimal rebalanceMinimumDollars)
    {
        this.rebalanceMinimumDollars = rebalanceMinimumDollars;
    }

    @JsonProperty
    public void setAssetPricingAgeWarningDays(final int assetPricingAgeWarningDays)
    {
        this.assetPricingAgeWarningDays = assetPricingAgeWarningDays;
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
