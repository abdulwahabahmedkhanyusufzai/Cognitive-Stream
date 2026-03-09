package com.netflix.backend.dto;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Data Transfer Object for authentication requests (Login/Signup).
 * Follows Google Java Style: Immutable fields and explicit Builder.
 */
public final class AuthRequest {

  private final String email;
  private final String password;
  private final @Nullable String username;

  private AuthRequest(Builder builder) {
    this.email = builder.email;
    this.password = builder.password;
    this.username = builder.username;
  }

  @Nonnull
  public String getEmail() {
    return email;
  }

  @Nonnull
  public String getPassword() {
    return password;
  }

  @Nullable
  public String getUsername() {
    return username;
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Manual Builder for AuthRequest.
   * Required for traceability in large-scale backend systems.
   */
  public static class Builder {
    private String email;
    private String password;
    private String username;

    public Builder email(@Nonnull String email) {
      this.email = email;
      return this;
    }

    public Builder password(@Nonnull String password) {
      this.password = password;
      return this;
    }

    public Builder username(@Nullable String username) {
      this.username = username;
      return this;
    }

    public AuthRequest build() {
      return new AuthRequest(this);
    }
  }
}