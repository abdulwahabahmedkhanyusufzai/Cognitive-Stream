package com.netflix.backend.config;

import com.netflix.backend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.annotation.Nonnull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter responsible for JWT authentication across various transport layers.
 * Extracts tokens from Cookies, Authorization headers, or Query parameters.
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

  private final UserDetailsService userDetailsService;
  private final JwtUtil jwtUtil;

  // Explicit constructor for maximum traceability in large monorepos
  public JwtRequestFilter(
      @Nonnull UserDetailsService userDetailsService, 
      @Nonnull JwtUtil jwtUtil) {
    this.userDetailsService = userDetailsService;
    this.jwtUtil = jwtUtil;
  }

  @Override
  protected void doFilterInternal(
      @Nonnull HttpServletRequest request,
      @Nonnull HttpServletResponse response,
      @Nonnull FilterChain chain)
      throws ServletException, IOException {
    
    String jwt = extractJwt(request);
    String username = null;

    if (jwt != null) {
      try {
        username = jwtUtil.getUsernameFromToken(jwt);
      } catch (Exception e) {
        // Structured logging is preferred for security monitoring
        logger.error("Failed to extract username from token", e);
      }
    }

    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      authenticateUser(request, jwt, username);
    }

    chain.doFilter(request, response);
  }

  /**
   * Orchestrates the multi-source extraction of the JWT.
   */
  private String extractJwt(@Nonnull HttpServletRequest request) {
    // 1. Cookie Extraction
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("jwt-netflix".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }

    // 2. Authorization Header Fallback
    final String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }

    // 3. Query Parameter Fallback (Useful for HLS/Streaming segments)
    return request.getParameter("token");
  }

  /**
   * Validates the token and sets the SecurityContext.
   */
  private void authenticateUser(
      @Nonnull HttpServletRequest request, 
      @Nonnull String jwt, 
      @Nonnull String username) {
    
    UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
    
    if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
      UsernamePasswordAuthenticationToken authToken = 
          new UsernamePasswordAuthenticationToken(
              userDetails, null, userDetails.getAuthorities());
      
      authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authToken);
    }
  }
}