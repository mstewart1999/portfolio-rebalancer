package com.pbalancer.client.service;

import com.pbalancer.client.Config;
import com.pbalancer.client.service.login.Credentials;
import com.pbalancer.client.service.login.LoginResponse;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class CentralLogin
{
    private static CentralLogin instance;

    public static synchronized CentralLogin getInstance()
    {
        if(instance == null)
        {
            instance = new CentralLogin();
        }
        return instance;
    }


    // TBD: self-signed TLS cert - https://stackoverflow.com/a/29078257
    private final Client client = ClientBuilder.newBuilder().build();
    private final WebTarget site = client.target(Config.getInstance().getCentralURL());
    private final WebTarget login = site.path("/login");

    private final String user;
    private final String pass;
    private String token;

    public CentralLogin(final String user, final String pass)
    {
        this.user = user;
        this.pass = pass;
    }

    public CentralLogin()
    {
        this(Config.getInstance().getCentralUser(), Config.getInstance().getCentralPass());
    }

    public synchronized String token() throws ServiceException
    {
        if(token == null)
        {
            try
            {
                Credentials credentials = new Credentials(user, pass);
                Response response = login
                        .request(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(credentials, MediaType.APPLICATION_JSON));
                if(response.getStatus() == Response.Status.OK.getStatusCode())
                {
                    LoginResponse out = response.readEntity(LoginResponse.class);
                    token = out.access_token();
                    //out.expires_in();
                }
                else
                {
                    throw new ServiceException(response.getStatusInfo().getReasonPhrase());
                }
            }
            catch (ProcessingException e)
            {
                throw new ServiceException("", e);
            }
        }
        return token;
    }

    public synchronized void invalidate()
    {
        token = null;
    }
}
