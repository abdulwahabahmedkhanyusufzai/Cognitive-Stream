package com.netflix.backend.repository;

import com.netflix.backend.model.User;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link User} entity persistence and query operations.
 * Follows Google Java Style: 2-space indentation and explicit null-safety.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

  /**
   * Retrieves a user by their unique email address.
   */
  @Nonnull
  Optional<User> findByEmail(@Nonnull String email);

  /**
   * Retrieves a user by their unique username.
   */
  @Nonnull
  Optional<User> findByUsername(@Nonnull String username);
}