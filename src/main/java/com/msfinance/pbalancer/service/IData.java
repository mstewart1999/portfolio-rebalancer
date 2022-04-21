package com.msfinance.pbalancer.service;

import java.io.IOException;
import java.util.List;

import com.msfinance.pbalancer.model.Portfolio;

public interface IData
{
    List<Portfolio> getPortfolios(String profileId) throws IOException;
    Portfolio getPortfolio(String profileId, String id) throws IOException;
    void createPortfolio(Portfolio p) throws IOException;
    void updatePortfolio(Portfolio p) throws IOException;
    void deletePortfolio(Portfolio p) throws IOException;
}
