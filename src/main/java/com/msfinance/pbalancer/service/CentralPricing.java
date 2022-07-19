package com.msfinance.pbalancer.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.msfinance.pbalancer.Config;
import com.msfinance.pbalancer.model.PriceResult;
import com.msfinance.pbalancer.service.login.Credentials;
import com.msfinance.pbalancer.service.login.LoginResponse;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class CentralPricing implements IPricing
{
    private static final String FREE_USER = Config.getInstance().getCentralUser();
    private static final String FREE_PASS = Config.getInstance().getCentralPass();

    // TBD: self-signed TLS cert - https://stackoverflow.com/a/29078257
    private final Client client = ClientBuilder.newBuilder().build();
    private final WebTarget site = client.target(Config.getInstance().getCentralURL());
    private final WebTarget login = site.path("/login");
    private final WebTarget api = site.path("/api/v1/tickerPricing");

    private String token;

    @Override
    public Map<String,PriceResult> getMostRecentEOD(final Collection<String> tickers) throws IOException
    {
        loginFree();

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
        else
        {
            throw new RuntimeException(response.getStatusInfo().getReasonPhrase());
        }
    }


    private static String commaSep(final Collection<String> tickers)
    {
        return String.join(",", tickers);
    }


    private void loginFree()
    {
        // TODO: retries? handle token expiration
        if(token == null)
        {
            Credentials credentials = new Credentials(FREE_USER, FREE_PASS);
            Response response = login
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(credentials, MediaType.APPLICATION_JSON));
            if(response.getStatus() == Response.Status.OK.getStatusCode())
            {
                LoginResponse out = response.readEntity(LoginResponse.class);
                token = out.access_token();
            }
            else
            {
                throw new RuntimeException(response.getStatusInfo().getReasonPhrase());
            }
        }
    }



    public static void main(final String[] args) throws IOException
    {
        Map<String, PriceResult> x = new CentralPricing().getMostRecentEOD(Arrays.asList("VTI", "VXUS", "BND"));
        System.out.println(x);
    }
}
