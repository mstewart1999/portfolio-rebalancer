package com.msfinance.pbalancer.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.msfinance.pbalancer.util.Validation;

public class Asset
{
    public enum PricingType { MANUAL_PER_UNIT, MANUAL_PER_WHOLE, AUTO_PER_UNIT }

    private final String accountId;
    private final String id;
    private String ticker;
    private String manualName;
    private String autoName;
    private int listPosition;
    private String assetClass;
    private PricingType pricingType;
    private BigDecimal units;
    private BigDecimal manualValue;
    private Date manualValueTmstp;
    private BigDecimal lastAutoValue;
    private Date lastAutoValueTmstp;

    private Account account;
    //private List<Basis> basis;

    public Asset(
            final String accountId
            )
    {
        this(accountId, UUID.randomUUID().toString());
    }

    @JsonCreator
    public Asset(
            @JsonProperty("accountId") final String accountId,
            @JsonProperty("id") final String id
            )
    {
        this.accountId = Objects.requireNonNull(accountId);
        this.id = Objects.requireNonNull(id);
        ticker = "";
        manualName = null;
        autoName = null;
        listPosition = 0;
        assetClass = null;
        pricingType = null;
        units = null;
        manualValue = null;
        manualValueTmstp = null;
        lastAutoValue = null;
        lastAutoValueTmstp = null;
        account = null;
        //basis = new ArrayList<>();
    }

    @JsonProperty
    public String getAccountId()
    {
        return accountId;
    }

    @JsonProperty
    public String getId()
    {
        return id;
    }

    @JsonProperty
    public String getTicker()
    {
        return ticker;
    }

    @JsonProperty
    public String getManualName()
    {
        return manualName;
    }

    @JsonProperty
    public String getAutoName()
    {
        return autoName;
    }

    @JsonProperty
    public int getListPosition()
    {
        return listPosition;
    }

    @JsonProperty
    public String getAssetClass()
    {
        return assetClass;
    }

    @JsonProperty
    public PricingType getPricingType()
    {
        return pricingType;
    }

    @JsonProperty
    public BigDecimal getUnits()
    {
        return units;
    }

    @JsonProperty
    public BigDecimal getManualValue()
    {
        return manualValue;
    }

    @JsonProperty
    public Date getManualValueTmstp()
    {
        return manualValueTmstp;
    }

    @JsonProperty
    public BigDecimal getLastAutoValue()
    {
        return lastAutoValue;
    }

    @JsonProperty
    public Date getLastAutoValueTmstp()
    {
        return lastAutoValueTmstp;
    }

    @JsonIgnore
    public Account getAccount()
    {
        return account;
    }

//    @JsonIgnore
//    public List<Basis> getBasis()
//    {
//        return basis;
//    }

    @JsonProperty
    public void setTicker(final String ticker)
    {
        this.ticker = ticker; // null allowed
    }

    @JsonProperty
    public void setManualName(final String manualName)
    {
        this.manualName = manualName; // null allowed
    }

    @JsonProperty
    public void setAutoName(final String autoName)
    {
        this.autoName = autoName; // null allowed
    }

    @JsonProperty
    public void setListPosition(final int listPosition)
    {
        this.listPosition = listPosition;
    }

    @JsonProperty
    public void setAssetClass(final String assetClass)
    {
        this.assetClass = assetClass;
    }

    @JsonProperty
    public void setPricingType(final PricingType pricingType)
    {
        this.pricingType = pricingType;
    }

    @JsonProperty
    public void setUnits(final BigDecimal units)
    {
        this.units = units;
    }

    @JsonProperty
    public void setManualValue(final BigDecimal manualValue)
    {
        this.manualValue = manualValue;
    }

    @JsonProperty
    public void setManualValueTmstp(final Date manualValueTmstp)
    {
        this.manualValueTmstp = manualValueTmstp;
    }

    @JsonProperty
    public void setLastAutoValue(final BigDecimal lastAutoValue)
    {
        this.lastAutoValue = lastAutoValue;
    }

    @JsonProperty
    public void setLastAutoValueTmstp(final Date lastAutoValueTmstp)
    {
        this.lastAutoValueTmstp = lastAutoValueTmstp;
    }

    @JsonIgnore
    public void setAccount(final Account account)
    {
        Validation.assertTrue(this.accountId.equals(account.getId()));
        this.account = account;
    }

//    @JsonIgnore
//    public void setBasis(final List<Basis> basis)
//    {
//        this.basis = Objects.requireNonNull(basis);
//    }

    @JsonIgnore
    public String getBestName()
    {
        if(manualName != null)
        {
            return manualName;
        }
        if(autoName != null)
        {
            return autoName;
        }
        return "";
    }

    @JsonIgnore
    public BigDecimal getBestTotalValue()
    {
        if((lastAutoValue != null) && (units != null))
        {
            return totalValue(units, lastAutoValue);
        }
        if(pricingType == PricingType.MANUAL_PER_WHOLE)
        {
            return manualValue;
        }
        if((pricingType == PricingType.MANUAL_PER_UNIT) && (manualValue != null) && (units != null))
        {
            return totalValue(units, manualValue);
        }
        return null;
    }

    public static BigDecimal totalValue(final BigDecimal units, final BigDecimal perUnitValue)
    {
        if((perUnitValue != null) && (units != null))
        {
            return perUnitValue.multiply(units).setScale(2, RoundingMode.HALF_UP);
        }
        return null;
    }
}
