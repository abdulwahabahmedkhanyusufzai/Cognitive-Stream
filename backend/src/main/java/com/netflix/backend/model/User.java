package com.netflix.backend.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Core User entity representing a Netflix profile.
 * Follows Google Java Style: 2-space indentation and explicit relationship handling.
 */
@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  private @Nullable String image;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "user_id")
  private List<SearchHistory> searchHistory = new ArrayList<>();

  // JPA requirement: No-args constructor
  public User() {}

  // Explicit constructor for creation (Google preference)
  public User(@Nonnull String username, @Nonnull String email, @Nonnull String password) {
    this.username = username;
    this.email = email;
    this.password = password;
  }

  // --- Getters ---
  
  public String getId() {
    return id;
  }

  @Nonnull
  public String getUsername() {
    return username;
  }

  @Nonnull
  public String getEmail() {
    return email;
  }

  // Password getter is present for the Auth Manager, but should be used sparingly
  public String getPassword() {
    return password;
  }

  @Nullable
  public String getImage() {
    return image;
  }

  public List<SearchHistory> getSearchHistory() {
    return searchHistory;
  }

  // --- Logic Helpers ---

  /**
   * Adds an item to search history while maintaining encapsulation.
   */
  public void addSearchEntry(@Nonnull SearchHistory entry) {
    this.searchHistory.add(0, entry); // Newest searches first
  }

  // --- Setters (Limited to maintain integrity) ---

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setImage(String image) {
    this.image = image;
  }
}