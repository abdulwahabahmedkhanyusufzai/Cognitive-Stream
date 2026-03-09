package com.netflix.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Entity representing a single entry in a user's search history.
 * Follows Google Java Style: 2-space indentation and explicit constructors.
 */
@Entity
@Table(name = "search_history")
public class SearchHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String contentId; // TMDB ID
  private String image;
  private String title;
  private String searchType;
  private LocalDateTime createdAt;

  // JPA requirement: No-args constructor
  public SearchHistory() {}

  // Explicit constructor for manual creation (Google preference)
  public SearchHistory(
      @Nonnull String contentId,
      @Nullable String image,
      @Nonnull String title,
      @Nonnull String searchType) {
    this.contentId = contentId;
    this.image = image;
    this.title = title;
    this.searchType = searchType;
    this.createdAt = LocalDateTime.now();
  }

  // Standard Getters (No setters to maintain semi-immutability)
  public Long getId() {
    return id;
  }

  public String getContentId() {
    return contentId;
  }

  public String getImage() {
    return image;
  }

  public String getTitle() {
    return title;
  }

  public String getSearchType() {
    return searchType;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
  
  // Explicit setters only where necessary for JPA (manually added if needed)
}