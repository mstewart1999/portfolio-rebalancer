package com.msfinance.pbalancer.service;

import java.io.IOException;
import java.util.List;

import com.msfinance.pbalancer.model.Portfolio;

public interface IData
{
    List<String> getPortfolioIds() throws IOException;
    Portfolio getPortfolio(String id) throws IOException;
    void createPortfolio(Portfolio p) throws IOException;
    void updatePortfolio(Portfolio currentPortfolio) throws IOException;
    void deletePortfolio(String id) throws IOException;
}
