package com.msfinance.pbalancer.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.msfinance.pbalancer.model.PortfolioAlert.Level;
import com.msfinance.pbalancer.model.PortfolioAlert.Type;
import com.msfinance.pbalancer.model.aa.AssetClass;
import com.msfinance.pbalancer.model.rebalance.TransactionSpecific;
import com.msfinance.pbalancer.util.Validation;

public class Asset implements IPersistable, Cloneable
{
    public enum PricingType { MANUAL_PER_UNIT, MANUAL_PER_WHOLE, AUTO_PER_UNIT, FIXED_PER_UNIT }
    public static final String CASH = "$CASH";

    private final String accountId;
    private final String id;
    private String ticker;
    private String manualName;
    private String autoName;
    private int listPosition;
    private String assetClass;
    private PricingType pricingType;
    private AssetProxy proxy;
    private BigDecimal units;
    private BigDecimal manualValue;
    private Date manualValueTmstp;
    private BigDecimal lastAutoValue;
    private Date lastAutoValueTmstp;

    private Account account;
    //private List<Basis> basis;
    private final List<PortfolioAlert> alerts;

    private boolean dirty = false;


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
        ticker = null;
        manualName = null;
        autoName = null;
        listPosition = 0;
        assetClass = null;
        pricingType = null;
        proxy = null;
        units = null;
        manualValue = null;
        manualValueTmstp = null;
        lastAutoValue = null;
        lastAutoValueTmstp = null;
        account = null;
        //basis = new ArrayList<>();
        alerts = new ArrayList<>();
    }

    @Override
    public Asset clone()
    {
        try
        {
            Asset copy = (Asset) super.clone();
            account = null; // caller should provide the appropriate cloned obj
            // basis TBD
            // alerts can be left as-is
            return copy;
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
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
    public AssetProxy getProxy()
    {
        return proxy;
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

    @JsonIgnore
    public List<PortfolioAlert> getAlerts()
    {
        return alerts;
    }

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
    public void setProxy(final AssetProxy proxy)
    {
        this.proxy = proxy;
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


    /**
     * Check for alerts.
     */
    public void validate()
    {
        alerts.clear();
        if(account == null)
        {
            alerts.add(new PortfolioAlert(Type.ASSET, Level.ERROR, "Account is undefined"));
        }
        if(pricingType == null)
        {
            alerts.add(new PortfolioAlert(Type.ASSET, Level.ERROR, "PricingType is undefined"));
        }
        if(units == null)
        {
            alerts.add(new PortfolioAlert(Type.ASSET, Level.ERROR, "Units is undefined"));
        }
        if(getBestTotalValue() == null)
        {
            alerts.add(new PortfolioAlert(Type.ASSET, Level.ERROR, "Total value is undefined"));
        }
        if(Validation.isBlank(getBestName()))
        {
            alerts.add(new PortfolioAlert(Type.ASSET, Level.ERROR, "Name is undefined"));
        }
        if(Validation.isBlank(assetClass))
        {
            alerts.add(new PortfolioAlert(Type.ASSET, Level.WARN, "AssetClass is blank"));
        }
        else if(AssetClass.UNDEFINED.equals(assetClass))
        {
            alerts.add(new PortfolioAlert(Type.ASSET, Level.WARN, "AssetClass is 'Undefined'"));
        }
        if((getBestTotalValue() != null) && Validation.almostEqual(getBestTotalValue().doubleValue(), 0.0, 0.001))
        {
            alerts.add(new PortfolioAlert(Type.ASSET, Level.INFO, "Total value is $0"));
        }
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
            // this case covers proxied assets also
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
        if((pricingType == PricingType.FIXED_PER_UNIT) && (manualValue != null) && (units != null))
        {
            return totalValue(units, manualValue);
        }
        return null;
    }

    @JsonIgnore
    public BigDecimal getBestUnitValue()
    {
        if(lastAutoValue != null)
        {
            // AUTO_PER_UNIT
            // proxied assets
            return lastAutoValue;
        }
        if(manualValue != null)
        {
            // MANUAL_PER_UNIT
            // FIXED_PER_UNIT
            // even MANUAL_PER_WHOLE (which doesn't fit well anywhere in this function)
            return manualValue;
        }
        // WTF?
        return BigDecimal.ZERO;
    }

    public static BigDecimal totalValue(final BigDecimal units, final BigDecimal perUnitValue)
    {
        if((perUnitValue != null) && (units != null))
        {
            return perUnitValue.multiply(units).setScale(2, RoundingMode.HALF_UP);
        }
        return null;
    }


    /**
     * Apply a (buy/sell) transaction to this asset. (modifies the object, so be careful)
     * @param t
     */
    public void apply(final TransactionSpecific t)
    {
        if((lastAutoValue != null) && (units != null))
        {
            // this case covers proxied assets also
            if(t.howMuchUnits() == null)
            {
                // not sure this case will ever occur
                BigDecimal howMuchUnits = new BigDecimal(t.howMuchDollars() / this.lastAutoValue.doubleValue());
                this.units = this.units.add(howMuchUnits);
            }
            else
            {
                this.units = this.units.add(new BigDecimal(t.howMuchUnits()));
            }
        }
        if(pricingType == PricingType.MANUAL_PER_WHOLE)
        {
            manualValue = manualValue.add(new BigDecimal(t.howMuchDollars()));
        }
        if((pricingType == PricingType.MANUAL_PER_UNIT) && (manualValue != null) && (units != null))
        {
            if(t.howMuchUnits() == null)
            {
                // not sure this case will ever occur
                BigDecimal howMuchUnits = new BigDecimal(t.howMuchDollars() / this.manualValue.doubleValue());
                this.units = this.units.add(howMuchUnits);
            }
            else
            {
                this.units = this.units.add(new BigDecimal(t.howMuchUnits()));
            }
        }
        if((pricingType == PricingType.FIXED_PER_UNIT) && (manualValue != null) && (units != null))
        {
            if(t.howMuchUnits() == null)
            {
                // not sure this case will ever occur
                BigDecimal howMuchUnits = new BigDecimal(t.howMuchDollars() / this.manualValue.doubleValue());
                this.units = this.units.add(howMuchUnits);
            }
            else
            {
                this.units = this.units.add(new BigDecimal(t.howMuchUnits()));
            }
        }
    }

    /**
     * Test whether the transaction
     * @param t
     * @return
     */
    public boolean isMatch(final TransactionSpecific t)
    {
        if((this.ticker != null) && this.ticker.equals(t.whatAsset()))
        {
            return true;
        }
        if((this.autoName != null) && this.autoName.equals(t.whatAsset()))
        {
            return true;
        }
        if((this.manualName != null) && this.manualName.equals(t.whatAsset()))
        {
            return true;
        }
        return false;
    }
}
