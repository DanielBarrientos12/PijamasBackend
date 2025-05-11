package com.neosoft.pijamasbakend.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.neosoft.pijamasbakend.security.JwtConfig.SECRET_KEY;

public class JwtValidationFilter extends BasicAuthenticationFilter {

    public JwtValidationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            Claims claims = Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token).getPayload();

            String email = claims.getSubject();
            String role  = claims.get("role", String.class);

            if (email != null && role != null) {
                // Construir el Authentication y poblar el contexto
                var authority = new SimpleGrantedAuthority(role);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(email, null, List.of(authority));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            chain.doFilter(request, response);
        } catch (JwtException ex) {
            // Token inválido o expirado → devuelve 401 y no sigue a otros filtros
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            var body = Map.of(
                    "error",   "invalid_token",
                    "message", ex.getMessage()
            );
            new ObjectMapper().writeValue(response.getWriter(), body);
        }

    }
}
