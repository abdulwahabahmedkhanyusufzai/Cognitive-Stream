package com.netflix.backend.service;

import com.netflix.backend.dto.AuthRequest;
import com.netflix.backend.dto.AuthResponse;
import com.netflix.backend.model.User;
import com.netflix.backend.repository.UserRepository;
import com.netflix.backend.util.JwtUtil;
import java.util.Random;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service handling user authentication, registration, and session token generation.
 * Follows Google Java Style: 2-space indentation and explicit dependency management.
 */
@Service
public class AuthService {

  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
  private static final Random RANDOM = new Random();
  private static final String[] PROFILE_PICS = {"/avatar1.png", "/avatar2.png", "/avatar3.png"};

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final AuthenticationManager authenticationManager;

  // Explicit constructor for maximum traceability in large monorepos
  public AuthService(
      @Nonnull UserRepository userRepository,
      @Nonnull PasswordEncoder passwordEncoder,
      @Nonnull JwtUtil jwtUtil,
      @Nonnull AuthenticationManager authenticationManager) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
    this.authenticationManager = authenticationManager;
  }

  public AuthResponse signup(@Nonnull AuthRequest request) {
    validateSignupRequest(request);

    String image = PROFILE_PICS[RANDOM.nextInt(PROFILE_PICS.length)];
    User user = new User(
        request.getUsername(), 
        request.getEmail(), 
        passwordEncoder.encode(request.getPassword())
    );
    user.setImage(image);

    userRepository.save(user);
    logger.info("New user registered: {}", user.getUsername());

    String token = jwtUtil.generateToken(user.getUsername());
    return AuthResponse.builder()
        .success(true)
        .user(user)
        .token(token)
        .build();
  }

  public AuthResponse login(@Nonnull AuthRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> {
          logger.warn("Login failed: User not found for email {}", request.getEmail());
          return new IllegalArgumentException("Invalid credentials");
        });

    String token = jwtUtil.generateToken(user.getUsername());
    return AuthResponse.builder()
        .success(true)
        .user(user)
        .token(token)
        .build();
  }

  private void validateSignupRequest(AuthRequest request) {
    if (request.getEmail() == null || request.getPassword() == null || request.getUsername() == null) {
      throw new IllegalArgumentException("All fields are required");
    }

    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new IllegalArgumentException("Email already exists");
    }

    if (userRepository.findByUsername(request.getUsername()).isPresent()) {
      throw new IllegalArgumentException("Username already exists");
    }

    if (request.getPassword().length() < 6) {
      throw new IllegalArgumentException("Password must be at least 6 characters");
    }
  }
}