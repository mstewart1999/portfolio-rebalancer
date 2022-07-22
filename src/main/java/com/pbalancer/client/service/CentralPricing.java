package com.pbalancer.client.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.pbalancer.client.Config;
import com.pbalancer.client.model.PriceResult;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class CentralPricing implements IPricing
{
    // TBD: self-signed TLS cert - https://stackoverflow.com/a/29078257
    private final Client client = ClientBuilder.newBuilder().build();
    private final WebTarget site = client.target(Config.getInstance().getCentralURL());
    private final WebTarget api = site.path("/api/v1/tickerPricing");

    private final CentralLogin login;

    public CentralPricing(final CentralLogin login)
    {
        this.login = login;
    }

    @Override
    public Map<String,PriceResult> getMostRecentEOD(final Collection<String> tickers) throws ServiceException
    {
        return getMostRecentEODWithRetries(tickers, 1, null);
    }

    private Map<String,PriceResult> getMostRecentEODWithRetries(final Collection<String> tickers, int retries, ServiceException lastException) throws ServiceException
    {
        if(retries == 0)
        {
            if(lastException == null)
            {
                lastException = new ServiceException("Call failed: getMostRecentEOD() - too many retries");
            }
            throw lastException;
        }

        String token = login.token();

        try
        {
            Response response = api
                    .queryParam("tickers", commaSep(tickers))
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .get();
            if(response.getStatus() == Response.Status.OK.getStatusCode())
            {
                ObjectMapper mapper = new ObjectMapper();
                TypeFactory typeFactory = mapper.getTypeFactory();
                MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, PriceResult.class);

                String json = response.readEntity(String.class);
                Map<String,PriceResult> out = mapper.readValue(json, mapType);
                return out;
            }
            else if(response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode() || response.getStatus() == Response.Status.FORBIDDEN.getStatusCode())
            {
                login.invalidate();
                return getMostRecentEODWithRetries(tickers, retries--, new ServiceException(response.getStatusInfo().getReasonPhrase()));
            }
            else
            {
                throw new ServiceException(response.getStatusInfo().getReasonPhrase());
            }
        }
        catch (ProcessingException|IOException e)
        {
            throw new ServiceException("Call failed: getMostRecentEOD()", e);
        }
    }


    private static String commaSep(final Collection<String> tickers)
    {
        return String.join(",", tickers);
    }


    public static void main(final String[] args) throws ServiceException
    {
        Map<String, PriceResult> x = new CentralPricing(CentralLogin.getInstance()).getMostRecentEOD(Arrays.asList("VTI", "VXUS", "BND"));
        System.out.println(x);
    }
}
