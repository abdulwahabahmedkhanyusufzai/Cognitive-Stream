package com.netflix.backend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility for generating and validating JSON Web Tokens (JWT).
 * Follows Google Java Style: 2-space indentation and immutable configuration.
 */
@Component
public class JwtUtil {

  private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
  
  private final String secret;
  private final long expiration;
  private final Key signInKey;

  // Explicit constructor for maximum traceability and immutability
  public JwtUtil(
      @Value("${jwt.secret}") String secret, 
      @Value("${jwt.expiration}") long expiration) {
    this.secret = secret;
    this.expiration = expiration;
    this.signInKey = initializeKey(secret);
  }

  @Nonnull
  public String getUsernameFromToken(@Nonnull String token) {
    return getClaimFromToken(token, Claims::getSubject);
  }

  @Nonnull
  public Date getExpirationDateFromToken(@Nonnull String token) {
    return getClaimFromToken(token, Claims::getExpiration);
  }

  public <T> T getClaimFromToken(@Nonnull String token, @Nonnull Function<Claims, T> claimsResolver) {
    final Claims claims = getAllClaimsFromToken(token);
    return claimsResolver.apply(claims);
  }

  public String generateToken(@Nonnull String username) {
    Map<String, Object> claims = new HashMap<>();
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(username)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(signInKey, SignatureAlgorithm.HS256)
        .compact();
  }

  public boolean validateToken(@Nonnull String token, @Nonnull String username) {
    try {
      final String tokenUsername = getUsernameFromToken(token);
      return (tokenUsername.equals(username) && !isTokenExpired(token));
    } catch (Exception e) {
      logger.warn("Token validation failed: {}", e.getMessage());
      return false;
    }
  }

  private Claims getAllClaimsFromToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(signInKey)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private boolean isTokenExpired(String token) {
    return getExpirationDateFromToken(token).before(new Date());
  }

  /**
   * Initializes the signing key. Ensures the secret is sufficiently strong.
   */
  private Key initializeKey(String secret) {
    try {
      byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(secret);
      return Keys.hmacShaKeyFor(keyBytes);
    } catch (Exception e) {
      logger.debug("Secret is not Base64 encoded, falling back to raw bytes.");
      return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
  }
}