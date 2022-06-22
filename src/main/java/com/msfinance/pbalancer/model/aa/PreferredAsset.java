package com.msfinance.pbalancer.model.aa;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.msfinance.pbalancer.model.IPersistable;
import com.msfinance.pbalancer.model.Portfolio;
import com.msfinance.pbalancer.model.PortfolioAlert;
import com.msfinance.pbalancer.model.PortfolioAlert.Level;
import com.msfinance.pbalancer.model.PortfolioAlert.Type;
import com.msfinance.pbalancer.util.Validation;

public class PreferredAsset implements IPersistable
{
    private final String portfolioId;
    private final String id;
    private int listPosition;
    private String assetClass;
    private String primaryAssetTicker;
    private String primaryAssetName;

    private Portfolio portfolio;
    private List<PortfolioAlert> alerts;

    private boolean dirty = false;


    public PreferredAsset(
            final String portfolioId
            )
    {
        this(portfolioId, UUID.randomUUID().toString());
    }

    @JsonCreator
    public PreferredAsset(
            @JsonProperty("portfolioId") final String portfolioId,
            @JsonProperty("id") final String id
            )
    {
        this.portfolioId = Objects.requireNonNull(portfolioId);
        this.id = Objects.requireNonNull(id);
        listPosition = 0;
        assetClass = null;
        primaryAssetTicker = null;
        primaryAssetName = null;
        portfolio = null;
        alerts = new ArrayList<>();
    }


    @JsonProperty
    public String getPortfolioId()
    {
        return portfolioId;
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
    public String getAssetClass()
    {
        return assetClass;
    }

    @JsonProperty
    public String getPrimaryAssetTicker()
    {
        return primaryAssetTicker;
    }

    @JsonProperty
    public String getPrimaryAssetName()
    {
        return primaryAssetName;
    }

    @JsonIgnore
    public Portfolio getPortfolio()
    {
        return portfolio;
    }

    @JsonIgnore
    public List<PortfolioAlert> getAlerts()
    {
        return alerts;
    }


    public void setListPosition(final int listPosition)
    {
        this.listPosition = listPosition;
    }

    public void setAssetClass(final String assetClass)
    {
        this.assetClass = assetClass;
    }

    public void setPrimaryAssetTicker(final String primaryAssetTicker)
    {
        this.primaryAssetTicker = primaryAssetTicker;
    }

    public void setPrimaryAssetName(final String primaryAssetName)
    {
        this.primaryAssetName = primaryAssetName;
    }

    public void setPortfolio(final Portfolio portfolio)
    {
        this.portfolio = portfolio;
    }

    public void setAlerts(final List<PortfolioAlert> alerts)
    {
        this.alerts = alerts;
    }



    public long countErrors()
    {
        return alerts
                .stream()
                .filter(a -> a.level() == PortfolioAlert.Level.ERROR)
                .count()
                ;
    }

    public long countWarns()
    {
        return alerts
                .stream()
                .filter(a -> a.level() == PortfolioAlert.Level.WARN)
                .count()
                ;
    }

    public long countInfos()
    {
        return alerts
                .stream()
                .filter(a -> a.level() == PortfolioAlert.Level.INFO)
                .count()
                ;
    }

    public void validate()
    {
        alerts.clear();
        if(Validation.isBlank(assetClass))
        {
            alerts.add(new PortfolioAlert(Type.ACM, Level.ERROR, "Asset class is missing"));
        }
        if(Validation.isBlank(primaryAssetTicker) && Validation.isBlank(primaryAssetName))
        {
            alerts.add(new PortfolioAlert(Type.ACM, Level.WARN, "Primary asset ticker/name is missing"));
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

}
