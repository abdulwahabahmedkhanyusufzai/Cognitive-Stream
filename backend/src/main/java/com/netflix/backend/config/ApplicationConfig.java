package com.netflix.backend.config;

import com.netflix.backend.repository.UserRepository;
import java.util.Collections;
import javax.annotation.Nonnull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails; // Import the interface
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration for the application's authentication and security providers.
 * Follows Google Java Style: 2-space indentation and interface-based returns.
 */
@Configuration
public class ApplicationConfig {

  private final UserRepository userRepository;

  public ApplicationConfig(@Nonnull UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Bean
  public UserDetailsService userDetailsService() {
    return identifier -> userRepository.findByUsername(identifier)
        .or(() -> userRepository.findByEmail(identifier))
        .map(this::mapToSpringSecurityUser)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier));
  }

  /**
   * Maps internal User entity to Spring Security UserDetails interface.
   * Note the return type is 'UserDetails' to prevent compilation errors.
   */
  private UserDetails mapToSpringSecurityUser(@Nonnull com.netflix.backend.model.User user) {
    return User.builder()
        .username(user.getUsername())
        .password(user.getPassword())
        .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
        .build();
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService());
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}