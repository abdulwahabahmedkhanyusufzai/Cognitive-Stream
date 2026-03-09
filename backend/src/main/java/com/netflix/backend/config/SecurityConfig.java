package com.netflix.backend.config;

import java.util.List;
import javax.annotation.Nonnull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Main security configuration for the application.
 * Configures the filter chain, CORS, and stateless session management.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtRequestFilter jwtAuthFilter;
  private final AuthenticationProvider authenticationProvider;

  // Explicit constructor for maximum traceability
  public SecurityConfig(
      @Nonnull JwtRequestFilter jwtAuthFilter,
      @Nonnull AuthenticationProvider authenticationProvider) {
    this.jwtAuthFilter = jwtAuthFilter;
    this.authenticationProvider = authenticationProvider;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .cors(cors -> cors.configurationSource(corsConfigurationSource()))
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        .requestMatchers("/api/v1/auth/**").permitAll()
        .anyRequest().authenticated())
      .sessionManagement(session -> session
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authenticationProvider(authenticationProvider)
      .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  /**
   * Helper method to modularize CORS configuration.
   */
  private CorsConfigurationSource corsConfigurationSource() {
    return request -> {
      CorsConfiguration config = new CorsConfiguration();
      config.setAllowedOriginPatterns(List.of("https://ba-frontend-v2.jollyfield-759e8655.uaenorth.azurecontainerapps.io"));
      config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
      config.setAllowedHeaders(List.of("*"));
      config.setAllowCredentials(true);
      return config;
    };
  }
}