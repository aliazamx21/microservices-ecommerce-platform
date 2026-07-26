package com.api_gateway.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    // This MUST match the secret in your Auth Service exactly
    private static final String SECRET_KEY = "my-super-secret-key";

    public DecodedJWT validateToken(String token) {
        return JWT.require(Algorithm.HMAC256(SECRET_KEY))
                .build()
                .verify(token);
    }
}