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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 2) Deshabilitamos CSRF (porque usamos JWT)
                .csrf(AbstractHttpConfigurer::disable)
                // 3) Regla de autorización por endpoint
                .authorizeHttpRequests(auth -> auth
                        // --- Public Endpoints ---
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/clientes/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/password-reset/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/facturas/checkout").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/api/facturas/wompi-webhook").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/facturas/webhook/wompi").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/productos/activos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/productos/categoria/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/productos/**").permitAll()

                        // Allow public access to category listings
                        .requestMatchers(HttpMethod.GET, "/api/categorias").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categorias/{id:[0-9]+}").permitAll()

                        // --- Swagger / API Docs ---
                        .requestMatchers(HttpMethod.GET, "/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/docs/swagger-ui.html", "/docs/swagger-ui/**").permitAll()

                        // --- Factura Endpoints ---
                        .requestMatchers(HttpMethod.GET, "/api/facturas/{id:[0-9]+}")
                        .hasAnyRole("CLIENTE", "ADMINISTRADOR", "GERENTE_VENTA")
                        .requestMatchers(HttpMethod.GET, "/api/facturas").hasAnyRole("ADMINISTRADOR", "GERENTE_VENTA")
                        .requestMatchers(HttpMethod.GET, "/api/facturas/cliente/{clienteId:[0-9]+}")
                        .hasAnyRole("CLIENTE", "ADMINISTRADOR", "GERENTE_VENTA")
                        .requestMatchers(HttpMethod.POST, "/api/facturas").hasRole("CLIENTE")

                        // --- Pedido Endpoints ---
                        .requestMatchers(HttpMethod.GET, "/api/pedidos").hasAnyRole("ADMINISTRADOR", "GERENTE_VENTA")
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/{id:[0-9]+}")
                        .hasAnyRole("CLIENTE", "ADMINISTRADOR", "GERENTE_VENTA")
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/cliente/{clienteId:[0-9]+}")
                        .hasAnyRole("CLIENTE", "ADMINISTRADOR", "GERENTE_VENTA")
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/{id:[0-9]+}/enviar")
                        .hasAnyRole("ADMINISTRADOR", "GERENTE_VENTA")
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/{id:[0-9]+}/entregar")
                        .hasAnyRole("ADMINISTRADOR", "GERENTE_VENTA")

                        // --- Product Management ---
                        .requestMatchers(HttpMethod.GET, "/api/productos/todos").hasAnyRole("ADMINISTRADOR", "PRODUCT_MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/productos/con-imagenes").hasAnyRole("ADMINISTRADOR", "PRODUCT_MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/productos").hasAnyRole("ADMINISTRADOR", "PRODUCT_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasAnyRole("ADMINISTRADOR", "PRODUCT_MANAGER")

                        // --- Promotion Management ---
                        .requestMatchers(HttpMethod.GET, "/api/promociones").hasAnyRole("ADMINISTRADOR", "PRODUCT_MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/promociones").hasAnyRole("ADMINISTRADOR", "PRODUCT_MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/promociones/*/productos").hasAnyRole("ADMINISTRADOR", "PRODUCT_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/promociones/**").hasAnyRole("ADMINISTRADOR", "PRODUCT_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/promociones/**").hasAnyRole("ADMINISTRADOR", "PRODUCT_MANAGER")

                        // --- Category, Subcategory, Size Management (write operations) ---
                        .requestMatchers(HttpMethod.POST, "/api/categorias/**").hasAnyRole("ADMINISTRADOR", "PRODUCT_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/categorias/**").hasAnyRole("ADMINISTRADOR", "PRODUCT_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/categorias/**").hasAnyRole("ADMINISTRADOR", "PRODUCT_MANAGER")
                        .requestMatchers("/api/subcategorias/**").hasAnyRole("ADMINISTRADOR", "PRODUCT_MANAGER")
                        .requestMatchers("/api/tallas/**").hasAnyRole("ADMINISTRADOR", "PRODUCT_MANAGER")

                        // --- Inventory ---
                        .requestMatchers(HttpMethod.POST, "/api/inventario").hasAnyRole("ADMINISTRADOR", "PRODUCT_MANAGER")

                        // --- Client Endpoints ---
                        .requestMatchers(HttpMethod.GET, "/api/clientes/{id:[0-9]+}")
                        .hasRole("CLIENTE")
                        .requestMatchers("/api/clientes/**")
                        .hasAnyRole("ADMINISTRADOR", "GERENTE_VENTA")

                        // --- Wompi / Payment ---
                        .requestMatchers("/api/wompi/tokens/**").permitAll()
                        .requestMatchers("/api/wompi/payment-sources/**").permitAll()

                        // --- Administrative Endpoints ---
                        .requestMatchers("/api/administrativos/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/roles/**").hasRole("ADMINISTRADOR")

                        // --- Fallback: authentication required ---
                        .anyRequest().authenticated()
                )
                // Nuestro filtro de login + filtro de validación
                .addFilter(new JwtAuthenticationFilter(authenticationManager()))
                .addFilter(new JwtValidationFilter(authenticationManager()));

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Sólo permitimos este origen cuando se termine la aplicacion
        //config.setAllowedOrigins(List.of("https://lucymundopijamas.store"));
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
