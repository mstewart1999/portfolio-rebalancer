package com.pbalancer.client.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AssetProxy
{
    private String proxyTicker;
    private String proxyAutoName;
    private BigDecimal privateAssetPriceComp;
    private BigDecimal proxyAssetPriceComp;
    private LocalDate pricingCompDate;
    private BigDecimal lastProxyValue;
    private Date lastProxyValueTmstp;

    public AssetProxy()
    {

    }

    @JsonProperty
    public String getProxyTicker()
    {
        return proxyTicker;
    }

    @JsonProperty
    public void setProxyTicker(final String proxyTicker)
    {
        this.proxyTicker = proxyTicker;
    }

    @JsonProperty
    public String getProxyAutoName()
    {
        return proxyAutoName;
    }

    @JsonProperty
    public void setProxyAutoName(final String proxyAutoName)
    {
        this.proxyAutoName = proxyAutoName;
    }

    @JsonProperty
    public BigDecimal getPrivateAssetPriceComp()
    {
        return privateAssetPriceComp;
    }

    @JsonProperty
    public void setPrivateAssetPriceComp(final BigDecimal privateAssetPriceComp)
    {
        this.privateAssetPriceComp = privateAssetPriceComp;
    }

    @JsonProperty
    public BigDecimal getProxyAssetPriceComp()
    {
        return proxyAssetPriceComp;
    }

    @JsonProperty
    public void setProxyAssetPriceComp(final BigDecimal proxyAssetPriceComp)
    {
        this.proxyAssetPriceComp = proxyAssetPriceComp;
    }

    @JsonProperty
    public LocalDate getPricingCompDate()
    {
        return pricingCompDate;
    }

    @JsonProperty
    public void setPricingCompDate(final LocalDate pricingCompDate)
    {
        this.pricingCompDate = pricingCompDate;
    }

    @JsonProperty
    public BigDecimal getLastProxyValue()
    {
        return lastProxyValue;
    }

    @JsonProperty
    public void setLastProxyValue(final BigDecimal lastProxyValue)
    {
        this.lastProxyValue = lastProxyValue;
    }

    @JsonProperty
    public Date getLastProxyValueTmstp()
    {
        return lastProxyValueTmstp;
    }

    @JsonProperty
    public void setLastProxyValueTmstp(final Date lastProxyValueTmstp)
    {
        this.lastProxyValueTmstp = lastProxyValueTmstp;
    }


    public BigDecimal calculate()
    {
        return lastProxyValue
                .multiply(privateAssetPriceComp)
                .divide(proxyAssetPriceComp, 2, RoundingMode.HALF_UP);
    }
}
