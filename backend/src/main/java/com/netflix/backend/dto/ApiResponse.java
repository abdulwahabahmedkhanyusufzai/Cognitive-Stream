package com.netflix.backend.dto;

import javax.annotation.Nullable;

/**
 * A generic wrapper for all API responses.
 * Follows Google Java Style: Immutable fields, 2-space indent, and manual Builder.
 */
public final class ApiResponse<T> {

  private final boolean success;
  private final @Nullable T content;
  private final @Nullable String message;

  private ApiResponse(Builder<T> builder) {
    this.success = builder.success;
    this.content = builder.content;
    this.message = builder.message;
  }

  public boolean isSuccess() {
    return success;
  }

  @Nullable
  public T getContent() {
    return content;
  }

  @Nullable
  public String getMessage() {
    return message;
  }

  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  /**
   * Manual Builder to replace Lombok @Builder for better traceability.
   */
  public static class Builder<T> {
    private boolean success;
    private T content;
    private String message;

    public Builder<T> success(boolean success) {
      this.success = success;
      return this;
    }

    public Builder<T> content(@Nullable T content) {
      this.content = content;
      return this;
    }

    public Builder<T> message(@Nullable String message) {
      this.message = message;
      return this;
    }

    public ApiResponse<T> build() {
      return new ApiResponse<>(this);
    }
  }
}