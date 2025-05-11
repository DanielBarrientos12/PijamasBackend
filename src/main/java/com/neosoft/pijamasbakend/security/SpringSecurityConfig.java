package com.neosoft.pijamasbakend.security;

import com.neosoft.pijamasbakend.filter.JwtAuthenticationFilter;
import com.neosoft.pijamasbakend.filter.JwtValidationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SpringSecurityConfig {

    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1) No mantenemos sesiones HTTP
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 2) Deshabilitamos CSRF (porque usamos JWT)
                .csrf(AbstractHttpConfigurer::disable)
                // 3) Regla de autorización por endpoint
                .authorizeHttpRequests(auth -> auth
                        // -- abrimos registro de clientes y la lectura pública de productos
                        .requestMatchers(HttpMethod.POST, "/api/clientes/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/productos/con-imagenes").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/productos/").permitAll()

                        // -- solo ADMINISTRADOR puede gestionar administrativos y roles
                        .requestMatchers("/api/administrativos/*").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/roles/**").hasRole("ADMINISTRADOR")

                        // -- Clientes: Administrador o Gerente de venta pueden consultar
                        .requestMatchers("/api/clientes/**")
                        .hasAnyRole("ADMINISTRADOR","GERENTE_VENTA")

                        // -- Categorías y subcategorías: sólo Administrador o Product Manager
                        .requestMatchers("/api/categorias/**")
                        .hasAnyRole("ADMINISTRADOR","PRODUCT_MANAGER")
                        .requestMatchers("/api/subcategorias/**")
                        .hasAnyRole("ADMINISTRADOR","PRODUCT_MANAGER")

                        // -- Tallas: Administrador o Product Manager
                        .requestMatchers("/api/tallas/**")
                        .hasAnyRole("ADMINISTRADOR","PRODUCT_MANAGER")

                        // -- Productos: lectura pública, pero creación/edición solo Product Manager o Administrador
                        .requestMatchers(HttpMethod.POST, "/api/productos/**")
                        .hasAnyRole("ADMINISTRADOR","PRODUCT_MANAGER")
                        .requestMatchers(HttpMethod.PUT,  "/api/productos/**")
                        .hasAnyRole("ADMINISTRADOR","PRODUCT_MANAGER")

                        // -- cualquier otro endpoint requiere autenticación
                        .anyRequest().authenticated()
                )
                // 4) Nuestro filtro de login + filtro de validación
                .addFilter(new JwtAuthenticationFilter(authenticationManager()))
                .addFilter(new JwtValidationFilter(authenticationManager()));

        return http.build();
    }

}
