package com.neosoft.pijamasbakend.security;

import io.jsonwebtoken.Jwts;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
public class JwtConfig {

    public static final SecretKey SECRET_KEY = Jwts.SIG.HS256.key().build();
    public static final long EXPIRATION_TIME_MS = 3600000;

}
