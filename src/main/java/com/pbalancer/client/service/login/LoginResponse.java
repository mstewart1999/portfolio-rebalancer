package com.pbalancer.client.service.login;

import java.util.List;

/* OAuth 2.0 RFC6749 access token response. */
public record LoginResponse(String access_token, String token_type, int expires_in, String refresh_token, String username, List<String> roles) {}
