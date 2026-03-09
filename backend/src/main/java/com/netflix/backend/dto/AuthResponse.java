package com.netflix.backend.dto;

import com.netflix.backend.model.User;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Data Transfer Object for authentication responses.
 * Follows Google Java Style: Immutable fields, 2-space indent, and explicit Builder.
 */
public final class AuthResponse {

  private final boolean success;
  private final @Nullable User user;
  private final @Nullable String token;
  private final @Nullable String message;

  private AuthResponse(Builder builder) {
    this.success = builder.success;
    this.user = builder.user;
    this.token = builder.token;
    this.message = builder.message;
  }

  public boolean isSuccess() {
    return success;
  }

  @Nullable
  public User getUser() {
    return user;
  }

  @Nullable
  public String getToken() {
    return token;
  }

  @Nullable
  public String getMessage() {
    return message;
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Manual Builder for AuthResponse.
   * Provides traceability for security-sensitive response data.
   */
  public static class Builder {
    private boolean success;
    private User user;
    private String token;
    private String message;

    public Builder success(boolean success) {
      this.success = success;
      return this;
    }

    public Builder user(@Nullable User user) {
      this.user = user;
      return this;
    }

    public Builder token(@Nullable String token) {
      this.token = token;
      return this;
    }

    public Builder message(@Nullable String message) {
      this.message = message;
      return this;
    }

    public AuthResponse build() {
      return new AuthResponse(this);
    }
  }
}