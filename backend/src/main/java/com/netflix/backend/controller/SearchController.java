package com.netflix.backend.controller;

import com.netflix.backend.model.SearchHistory;
import com.netflix.backend.model.User;
import com.netflix.backend.repository.UserRepository;
import com.netflix.backend.service.TmdbService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling multi-type search operations and user search history.
 * Follows Google Java Style: 2-space indentation and explicit logging.
 */
@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

  private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
  private static final String TMDB_SEARCH_BASE = "https://api.themoviedb.org/3/search/";
  private static final String SEARCH_PARAMS = "&include_adult=false&language=en-US&page=1";

  private final TmdbService tmdbService;
  private final UserRepository userRepository;

  // Explicit constructor for maximum traceability
  public SearchController(@Nonnull TmdbService tmdbService, @Nonnull UserRepository userRepository) {
    this.tmdbService = tmdbService;
    this.userRepository = userRepository;
  }

  @GetMapping("/person/{query}")
  public ResponseEntity<Map<String, Object>> searchPerson(
      @Nonnull @PathVariable String query,
      @AuthenticationPrincipal UserDetails userDetails) {
    String url = String.format("%sperson?query=%s%s", TMDB_SEARCH_BASE, query, SEARCH_PARAMS);
    return searchAndSave(userDetails.getUsername(), query, "person", url);
  }

  @GetMapping("/movie/{query}")
  public ResponseEntity<Map<String, Object>> searchMovie(
      @Nonnull @PathVariable String query,
      @AuthenticationPrincipal UserDetails userDetails) {
    String url = String.format("%smovie?query=%s%s", TMDB_SEARCH_BASE, query, SEARCH_PARAMS);
    return searchAndSave(userDetails.getUsername(), query, "movie", url);
  }

  @GetMapping("/tv/{query}")
  public ResponseEntity<Map<String, Object>> searchTv(
      @Nonnull @PathVariable String query,
      @AuthenticationPrincipal UserDetails userDetails) {
    String url = String.format("%stv?query=%s%s", TMDB_SEARCH_BASE, query, SEARCH_PARAMS);
    return searchAndSave(userDetails.getUsername(), query, "tv", url);
  }

  @GetMapping("/history")
  public ResponseEntity<Map<String, Object>> getSearchHistory(
      @AuthenticationPrincipal UserDetails userDetails) {
    return userRepository.findByUsername(userDetails.getUsername())
        .map(user -> createSuccessResponse("content", user.getSearchHistory()))
        .orElseGet(() -> createErrorResponse(404, "User not found"));
  }

  @DeleteMapping("/history/{id}")
  public ResponseEntity<Map<String, Object>> removeFromHistory(
      @Nonnull @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    try {
      User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
      if (user == null) {
        return createErrorResponse(404, "User not found");
      }

      boolean removed = user.getSearchHistory().removeIf(item -> item.getId().equals(id));
      if (removed) {
        userRepository.save(user);
        return createSuccessResponse("message", "Item removed from search history");
      }
      return createErrorResponse(404, "Item not found");

    } catch (Exception e) {
      logger.error("Failed to remove history item ID: {}", id, e);
      return createErrorResponse(500, "Internal Server Error");
    }
  }

  /**
   * Internal helper to perform search and update user history.
   */
  private ResponseEntity<Map<String, Object>> searchAndSave(
      String username, String query, String type, String url) {
    try {
      Map<String, Object> data = tmdbService.fetchFromTmdb(url);
      if (data == null) {
        return ResponseEntity.notFound().build();
      }

      @SuppressWarnings("unchecked")
      List<Map<String, Object>> results = (List<Map<String, Object>>) data.get("results");

      if (results != null && !results.isEmpty()) {
        updateUserHistory(username, results.get(0), type);
      }

      return createSuccessResponse("content", results);

    } catch (Exception e) {
      logger.error("Search failed for user: {} with query: {}", username, query, e);
      return createErrorResponse(500, "Internal Server Error");
    }
  }

  /**
   * Helper to map TMDB results to our SearchHistory entity.
   */
 private void updateUserHistory(String username, Map<String, Object> firstResult, String type) {
    userRepository.findByUsername(username).ifPresent(user -> {
      // Determine title and image based on type
      String title;
      String image;

      if ("person".equals(type)) {
        title = (String) firstResult.get("name");
        image = (String) firstResult.get("profile_path");
      } else {
        title = (String) firstResult.get("movie".equals(type) ? "title" : "name");
        image = (String) firstResult.get("poster_path");
      }

      // Use the explicit constructor we created in the SearchHistory entity
      SearchHistory item = new SearchHistory(
          String.valueOf(firstResult.get("id")),
          image,
          title,
          type
      );

      // In the entity, we should also have a method to add this to the list
      user.getSearchHistory().add(0, item);
      userRepository.save(user);
    });
  }

  private ResponseEntity<Map<String, Object>> createSuccessResponse(String key, Object content) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put(key, content);
    return ResponseEntity.ok(response);
  }

  private ResponseEntity<Map<String, Object>> createErrorResponse(int status, String message) {
    return ResponseEntity.status(status).body(Map.of("success", false, "message", message));
  }
}