package com.netflix.backend.controller;

import com.netflix.backend.dto.AuthRequest;
import com.netflix.backend.dto.AuthResponse;
import com.netflix.backend.model.User;
import com.netflix.backend.repository.UserRepository;
import com.netflix.backend.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling authentication requests including signup, login, and session validation.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
  private static final String JWT_COOKIE_NAME = "jwt-netflix";
  private static final long COOKIE_EXPIRATION_SECONDS = 15 * 24 * 60 * 60; // 15 days

  private final AuthService authService;
  private final UserRepository userRepository;

  // Explicit constructor for maximum traceability in large-scale monorepos
  public AuthController(
      @Nonnull AuthService authService, 
      @Nonnull UserRepository userRepository) {
    this.authService = authService;
    this.userRepository = userRepository;
  }

  @PostMapping("/signup")
  public ResponseEntity<AuthResponse> signup(
      @Nonnull @RequestBody AuthRequest request, 
      @Nonnull HttpServletResponse response) {
    logger.info("Signup request received for email: {}", request.getEmail());
    try {
      AuthResponse authResponse = authService.signup(request);
      logger.info("Signup successful for user: {}", request.getUsername());
      setJwtCookie(response, authResponse.getToken());
      return ResponseEntity.ok(authResponse);
    } catch (Exception e) {
      logger.error("Signup failed for email {}: {}", request.getEmail(), e.getMessage());
      throw e;
    }
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(
      @Nonnull @RequestBody AuthRequest request, 
      @Nonnull HttpServletResponse response) {
    logger.info("Login request received for user: {}", request.getEmail());
    try {
      AuthResponse authResponse = authService.login(request);
      logger.info("Login successful for user: {}", request.getEmail());
      setJwtCookie(response, authResponse.getToken());
      return ResponseEntity.ok(authResponse);
    } catch (Exception e) {
      logger.error("Login failed for user {}: {}", request.getEmail(), e.getMessage());
      throw e;
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<AuthResponse> logout(@Nonnull HttpServletResponse response) {
    clearJwtCookie(response);
    return ResponseEntity.ok(
        AuthResponse.builder()
            .success(true)
            .message("Logged out successfully")
            .build());
  }

  @GetMapping("/authCheck")
  public ResponseEntity<AuthResponse> authCheck(
      @AuthenticationPrincipal UserDetails userDetails) {
    if (userDetails == null) {
      logger.warn("authCheck: UserDetails is null - Unauthorized");
      return ResponseEntity.status(401)
          .body(AuthResponse.builder().success(false).message("Unauthorized").build());
    }

    logger.info("authCheck success for user: {}", userDetails.getUsername());
    User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
    return ResponseEntity.ok(
        AuthResponse.builder()
            .success(true)
            .user(user)
            .build());
  }

  /**
   * Sets a secure, HttpOnly cookie containing the JWT.
   */
  private void setJwtCookie(HttpServletResponse response, String token) {
    String cookieHeader = String.format(
        "%s=%s; Path=/; Max-Age=%d; HttpOnly; Secure; SameSite=None",
        JWT_COOKIE_NAME,
        token,
        COOKIE_EXPIRATION_SECONDS
    );
    response.addHeader("Set-Cookie", cookieHeader);
  }

  /**
   * Clears the JWT cookie by setting Max-Age to 0.
   */
  private void clearJwtCookie(HttpServletResponse response) {
    String cookieHeader = String.format(
        "%s=; Path=/; Max-Age=0; HttpOnly; Secure; SameSite=None", 
        JWT_COOKIE_NAME
    );
    response.addHeader("Set-Cookie", cookieHeader);
  }
}