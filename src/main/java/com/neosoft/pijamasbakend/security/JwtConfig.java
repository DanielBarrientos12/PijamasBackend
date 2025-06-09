package com.neosoft.pijamasbakend.security;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
public class JwtConfig {

    private static final String SECRET_BASE64 =
            "gS8l/4vQ7ZYJ2y6qN3EjVb++0O3P2K5Yb8H1Z2N9mMw=";

    public static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
            Decoders.BASE64.decode(SECRET_BASE64)
    );
    public static final long EXPIRATION_TIME_MS = 3600000;

}
