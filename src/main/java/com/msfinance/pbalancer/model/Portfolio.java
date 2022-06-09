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
import com.msfinance.pbalancer.model.Asset.PricingType;
import com.msfinance.pbalancer.model.PortfolioAlert.Level;
import com.msfinance.pbalancer.model.PortfolioAlert.Type;
import com.msfinance.pbalancer.model.aa.AssetAllocation;
import com.msfinance.pbalancer.model.rebalance.TransactionSpecific;
import com.msfinance.pbalancer.util.Validation;

public class Portfolio implements IPersistable, Cloneable
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

    @Override
    public Portfolio clone()
    {
        try
        {
            Portfolio copy = (Portfolio) super.clone();
            copy.profile = null; // caller should provide the appropriate cloned obj
            copy.accounts = new ArrayList<>();
            for(Account a : this.accounts)
            {
                Account aCopy = a.clone();
                aCopy.setPortfolio(copy);
                copy.accounts.add(aCopy);
            }
            // targetAA is not modified for our purposes, so leave the old one in place
            //copy.targetAA = this.targetAA.clone();
            return copy;
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
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

    @JsonIgnore
    public List<PortfolioAlert> getAccountAlerts()
    {
        // dynamically generate this each time
        List<PortfolioAlert> alerts = new ArrayList<>();

        long errorAlerts = countAccountErrors();
        long warnAlerts = countAccountWarns();
        long infoAlerts = countAccountInfos();
        if(errorAlerts > 0)
        {
            alerts.add(new PortfolioAlert(Type.PORTFOLIO, Level.ERROR, "Account errors: " + errorAlerts));
        }
        if(warnAlerts > 0)
        {
            alerts.add(new PortfolioAlert(Type.PORTFOLIO, Level.WARN, "Account warnings: " + warnAlerts));
        }
        if(infoAlerts > 0)
        {
            alerts.add(new PortfolioAlert(Type.PORTFOLIO, Level.INFO, "Account infos: " + infoAlerts));
        }

        errorAlerts = countTAAErrors();
        warnAlerts = countTAAWarns();
        infoAlerts = countTAAInfos();
        if(errorAlerts > 0)
        {
            alerts.add(new PortfolioAlert(Type.PORTFOLIO, Level.ERROR, "Target Asset Allocation errors: " + errorAlerts));
        }
        if(warnAlerts > 0)
        {
            alerts.add(new PortfolioAlert(Type.PORTFOLIO, Level.WARN, "Target Asset Allocation warnings: " + warnAlerts));
        }
        if(infoAlerts > 0)
        {
            alerts.add(new PortfolioAlert(Type.PORTFOLIO, Level.INFO, "Target Asset Allocation infos: " + infoAlerts));
        }

        return alerts;
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


    public long countAccountErrors()
    {
        return accounts
          .stream()
          .map(a -> a.countErrors())
          .filter(c -> c > 0)
          .count()
          ;
    }

    public long countAccountWarns()
    {
        return accounts
                .stream()
                .map(a -> a.countWarns())
                .filter(c -> c > 0)
                .count()
                ;
    }

    public long countAccountInfos()
    {
        return accounts
                .stream()
                .map(a -> a.countInfos())
                .filter(c -> c > 0)
                .count()
                ;
    }


    public long countTAAErrors()
    {
        return getTargetAA().countErrors();
    }

    public long countTAAWarns()
    {
        return getTargetAA().countWarns();
    }

    public long countTAAInfos()
    {
        return getTargetAA().countInfos();
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


    /**
     * Simulate performing a list of transactions on a portfolio.
     * @param trans the transactions
     * @return a copy of the portfolio with transactions applied
     */
    public Portfolio apply(final List<TransactionSpecific> trans)
    {
        Portfolio copy = this.clone();
        for(TransactionSpecific t : trans)
        {
            for(Account acct : copy.accounts)
            {
                if(acct.getId().equals(t.where().getId()))
                {
                    boolean hadMatchAlready = false;
                    for(Asset a : acct.getAssets())
                    {
                        if(!hadMatchAlready && a.isMatch(t))
                        {
                            a.apply(t);
                            // in case of multiple matches, only apply to the first
                            hadMatchAlready = true;
                        }
                    }
                    if(!hadMatchAlready)
                    {
                        // create a simplified dummy asset and apply the transaction to it
                        Asset newAsset = new Asset(acct.getId());
                        newAsset.setAccount(acct);
                        newAsset.setAssetClass(t.assetClass());
                        newAsset.setManualName(t.whatAsset()); // this could be a ticker, but doesn't matter for our use case
                        // for this dummy asset - just restrict to 2 cases of manual assets
                        if(t.howMuchUnits() == null)
                        {
                            newAsset.setPricingType(PricingType.MANUAL_PER_WHOLE);
                            newAsset.setManualValue(BigDecimal.ZERO);
                            newAsset.setUnits(BigDecimal.ONE);
                        }
                        else
                        {
                            newAsset.setPricingType(PricingType.MANUAL_PER_UNIT);
                            newAsset.setManualValue(BigDecimal.ZERO);
                            newAsset.setUnits(BigDecimal.ZERO);
                        }
                        newAsset.apply(t);
                        hadMatchAlready = true;
                        acct.getAssets().add(newAsset);
                    }
                }
            }
        }
        return copy;
    }
}
