package com.netflix.backend.controller;

import com.netflix.backend.service.TmdbService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for movie-related operations, interfacing with TMDB metadata.
 * Follows Google Java Style: 2-space indentation and explicit constructors.
 */
@RestController
@RequestMapping("/api/v1/movie")
public class MovieController {

  private static final Logger logger = LoggerFactory.getLogger(MovieController.class);
  private final TmdbService tmdbService;
  private final Random random = new Random();

  // Explicit constructor for maximum traceability
  public MovieController(@Nonnull TmdbService tmdbService) {
    this.tmdbService = tmdbService;
  }

  @GetMapping("/trending")
  public ResponseEntity<Map<String, Object>> getTrendingMovie() {
    try {
      Map<String, Object> data = tmdbService.fetchFromTmdb(
          "https://api.themoviedb.org/3/trending/movie/day?language=en-US");

      if (data == null || !data.containsKey("results")) {
        logger.error("TMDB Trending API returned invalid data structure");
        return createErrorResponse("Internal Server Error");
      }

      @SuppressWarnings("unchecked")
      List<Map<String, Object>> results = (List<Map<String, Object>>) data.get("results");
      
      if (results.isEmpty()) {
        return createErrorResponse("No trending movies found");
      }

      Map<String, Object> randomMovie = results.get(random.nextInt(results.size()));
      return createSuccessResponse("content", randomMovie);

    } catch (Exception e) {
      logger.error("Exception in getTrendingMovie", e);
      return createErrorResponse("Internal Server Error");
    }
  }

  @GetMapping("/{id}/trailers")
  public ResponseEntity<Map<String, Object>> getMovieTrailers(@Nonnull @PathVariable String id) {
    try {
      String url = String.format("https://api.themoviedb.org/3/movie/%s/videos?language=en-US", id);
      Map<String, Object> data = tmdbService.fetchFromTmdb(url);
      
      if (data == null) {
        logger.warn("No trailer data found for movie ID: {}", id);
        return ResponseEntity.notFound().build();
      }

      return createSuccessResponse("trailers", data.get("results"));
    } catch (Exception e) {
      logger.error("Exception fetching trailers for movie ID: {}", id, e);
      return createErrorResponse("Internal Server Error");
    }
  }

  @GetMapping("/{id}/details")
  public ResponseEntity<Map<String, Object>> getMovieDetails(@Nonnull @PathVariable String id) {
    try {
      String url = String.format("https://api.themoviedb.org/3/movie/%s?language=en-US", id);
      Map<String, Object> data = tmdbService.fetchFromTmdb(url);

      if (data == null) {
        return ResponseEntity.notFound().build();
      }

      return createSuccessResponse("content", data);
    } catch (Exception e) {
      logger.error("Exception fetching details for movie ID: {}", id, e);
      return createErrorResponse("Internal Server Error");
    }
  }

  @GetMapping("/{id}/similar")
  public ResponseEntity<Map<String, Object>> getSimilarMovies(@Nonnull @PathVariable String id) {
    try {
      String url = String.format(
          "https://api.themoviedb.org/3/movie/%s/similar?language=en-US&page=1", id);
      Map<String, Object> data = tmdbService.fetchFromTmdb(url);

      if (data == null) {
        return ResponseEntity.notFound().build();
      }

      return createSuccessResponse("similar", data.get("results"));
    } catch (Exception e) {
      logger.error("Exception fetching similar movies for ID: {}", id, e);
      return createErrorResponse("Internal Server Error");
    }
  }

  @GetMapping("/{category}")
  public ResponseEntity<Map<String, Object>> getMoviesByCategory(@Nonnull @PathVariable String category) {
    try {
      String url = String.format(
          "https://api.themoviedb.org/3/movie/%s?language=en-US&page=1", category);
      Map<String, Object> data = tmdbService.fetchFromTmdb(url);

      if (data == null) {
        return ResponseEntity.notFound().build();
      }

      return createSuccessResponse("content", data.get("results"));
    } catch (Exception e) {
      logger.error("Exception fetching movies for category: {}", category, e);
      return createErrorResponse("Internal Server Error");
    }
  }

  // Private helper methods to standardize API responses
  private ResponseEntity<Map<String, Object>> createSuccessResponse(String key, Object content) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put(key, content);
    return ResponseEntity.ok(response);
  }

  private ResponseEntity<Map<String, Object>> createErrorResponse(String message) {
    return ResponseEntity.internalServerError()
        .body(Map.of("success", false, "message", message));
  }
}