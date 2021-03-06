package com.pbalancer.client.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pbalancer.client.model.Asset.PricingType;
import com.pbalancer.client.model.PortfolioAlert.Level;
import com.pbalancer.client.model.PortfolioAlert.Type;
import com.pbalancer.client.model.aa.AssetAllocation;
import com.pbalancer.client.model.aa.DefaultPreferredAsset;
import com.pbalancer.client.model.aa.DefaultPreferredAssetCache;
import com.pbalancer.client.model.aa.PreferredAsset;
import com.pbalancer.client.model.rebalance.TransactionSpecific;
import com.pbalancer.client.util.DateHelper;
import com.pbalancer.client.util.Validation;

public class Portfolio implements IPersistable, Cloneable
{
    private final String profileId;
    private final String id;
    private int listPosition;
    private String name;
    private PortfolioGoal goal;
    private BigDecimal lastValue;
    private Date lastValueTmstp;
    private Date lastValueTmstpLow;

    private Profile profile;
    private List<Account> accounts;
    private AssetAllocation targetAA;
    private List<PreferredAsset> assetClassMappings;

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
        lastValueTmstpLow = null;
        profile = null;
        accounts = new ArrayList<>();
        targetAA = new AssetAllocation();
        assetClassMappings = new ArrayList<>();
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

            // assetClassMapping is not modified for our purposes, so leave the old one in place
            //copy.assetClassMapping = this.assetClassMapping.clone();

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
    public List<PreferredAsset> getAssetClassMappings()
    {
        return assetClassMappings;
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

        errorAlerts = countACMErrors();
        warnAlerts = countACMWarns();
        infoAlerts = countACMInfos();
        if(errorAlerts > 0)
        {
            alerts.add(new PortfolioAlert(Type.PORTFOLIO, Level.ERROR, "Preferred Investment errors: " + errorAlerts));
        }
        if(warnAlerts > 0)
        {
            alerts.add(new PortfolioAlert(Type.PORTFOLIO, Level.WARN, "Preferred Investment warnings: " + warnAlerts));
        }
        if(infoAlerts > 0)
        {
            alerts.add(new PortfolioAlert(Type.PORTFOLIO, Level.INFO, "Preferred Investment infos: " + infoAlerts));
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

    @JsonProperty
    public void setLastValueTmstpLow(final Date lastValueTmstpLow)
    {
        this.lastValueTmstpLow = lastValueTmstpLow;
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

    @JsonProperty
    public void setTargetAA(final AssetAllocation targetAA)
    {
        this.targetAA = Objects.requireNonNull(targetAA);
    }

    @JsonIgnore
    public void setAssetClassMappings(final List<PreferredAsset> assetClassMappings)
    {
        this.assetClassMappings = Objects.requireNonNull(assetClassMappings);
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


    public long countACMErrors()
    {
        return assetClassMappings
                .stream()
                .map(m -> m.countErrors())
                .filter(c -> c > 0)
                .count()
                ;
    }

    public long countACMWarns()
    {
        return assetClassMappings
                .stream()
                .map(m -> m.countWarns())
                .filter(c -> c > 0)
                .count()
                ;
    }

    public long countACMInfos()
    {
        return assetClassMappings
                .stream()
                .map(m -> m.countInfos())
                .filter(c -> c > 0)
                .count()
                ;
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
        copy.setProfile(this.getProfile());
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

    /**
     * Verify that the set of asset class mappings cover the needed asset classes.
     * @return new asset class mappings that must be persisted
     */
    public List<PreferredAsset> validateAssetClassMappings()
    {
        List<String> needed = targetAA.getRoot().allLeaves().stream().map(l -> l.getName()).toList();
        needed = new ArrayList<>(needed); // modifiable copy
        Set<String> found = new HashSet<>();
        Set<String> duplicates = new HashSet<>();

        for(PreferredAsset acm : assetClassMappings)
        {
            acm.validate();

            String ac = acm.getAssetClass();
            if(needed.contains(ac))
            {
                needed.remove(ac);
            }
            else
            {
                acm.getAlerts().add(new PortfolioAlert(Type.ACM, Level.INFO, "Unnecessary mapping"));
            }

            if(found.contains(ac))
            {
                duplicates.add(ac);
            }
            found.add(ac);
        }
        // warn about duplicates
        for(PreferredAsset acm : assetClassMappings)
        {
            String ac = acm.getAssetClass();
            if(duplicates.contains(ac))
            {
                acm.getAlerts().add(new PortfolioAlert(Type.ACM, Level.WARN, "Duplicate mapping"));
            }
        }

        int currMaxListPosition = assetClassMappings
                .stream()
                .map(c -> c.getListPosition())
                .max((i, j) -> i.compareTo(j))
                .orElse(0);
        int listPosition = currMaxListPosition + 1;

        List<PreferredAsset> created = new ArrayList<>();
        for(String ac : needed)
        {
            PreferredAsset acm = new PreferredAsset(this.getId());
            acm.setPortfolio(this);
            assetClassMappings.add(acm);
            acm.setAssetClass(ac);
            acm.setListPosition(listPosition++);
            {
                DefaultPreferredAsset dpa = DefaultPreferredAssetCache.getInstance().lookupPrimary(ac);
                if(dpa != null)
                {
                    acm.setPrimaryAssetTicker(dpa.getTicker());
                    acm.setPrimaryAssetName(dpa.getName());
                }
            }
            acm.markDirty();

            created.add(acm);

            acm.validate();
        }
        return created;
    }

    public PreferredAsset lookupPreferredAsset(final String desiredAssetClass)
    {
        for(PreferredAsset acm : assetClassMappings)
        {
            String ac = acm.getAssetClass();
            if(ac.equals(desiredAssetClass))
            {
                return acm;
            }
        }
        return null;
    }
}
