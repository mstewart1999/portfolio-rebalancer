package com.msfinance.pbalancer.model;

import java.math.BigDecimal;
import java.util.Date;

public record PriceResult(String ticker, BigDecimal price, Date when)
{
}
