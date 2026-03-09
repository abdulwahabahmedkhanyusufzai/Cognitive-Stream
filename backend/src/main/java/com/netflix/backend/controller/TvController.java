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
 * Controller for TV show operations, interfacing with TMDB metadata.
 * Follows Google Java Style: 2-space indentation and explicit constructors.
 */
@RestController
@RequestMapping("/api/v1/tv")
public class TvController {

  private static final Logger logger = LoggerFactory.getLogger(TvController.class);
  private static final String TMDB_TV_BASE = "https://api.themoviedb.org/3/tv/";
  
  private final TmdbService tmdbService;
  private final Random random = new Random();

  // Explicit constructor for maximum traceability
  public TvController(@Nonnull TmdbService tmdbService) {
    this.tmdbService = tmdbService;
  }

  @GetMapping("/trending")
  public ResponseEntity<Map<String, Object>> getTrendingTv() {
    try {
      Map<String, Object> data = tmdbService.fetchFromTmdb(
          "https://api.themoviedb.org/3/trending/tv/day?language=en-US");

      if (data == null || !data.containsKey("results")) {
        logger.error("TMDB Trending TV API returned invalid data");
        return createErrorResponse("Internal Server Error");
      }

      @SuppressWarnings("unchecked")
      List<Map<String, Object>> results = (List<Map<String, Object>>) data.get("results");
      
      if (results.isEmpty()) {
        return createErrorResponse("No trending TV shows found");
      }

      Map<String, Object> randomTv = results.get(random.nextInt(results.size()));
      return createSuccessResponse("content", randomTv);

    } catch (Exception e) {
      logger.error("Exception in getTrendingTv", e);
      return createErrorResponse("Internal Server Error");
    }
  }

  @GetMapping("/{id}/trailers")
  public ResponseEntity<Map<String, Object>> getTvTrailers(@Nonnull @PathVariable String id) {
    try {
      String url = String.format("%s%s/videos?language=en-US", TMDB_TV_BASE, id);
      Map<String, Object> data = tmdbService.fetchFromTmdb(url);
      
      if (data == null) {
        logger.warn("No trailer data found for TV ID: {}", id);
        return ResponseEntity.notFound().build();
      }

      return createSuccessResponse("trailers", data.get("results"));
    } catch (Exception e) {
      logger.error("Exception fetching trailers for TV ID: {}", id, e);
      return createErrorResponse("Internal Server Error");
    }
  }

  @GetMapping("/{id}/details")
  public ResponseEntity<Map<String, Object>> getTvDetails(@Nonnull @PathVariable String id) {
    try {
      String url = String.format("%s%s?language=en-US", TMDB_TV_BASE, id);
      Map<String, Object> data = tmdbService.fetchFromTmdb(url);

      if (data == null) {
        return ResponseEntity.notFound().build();
      }

      return createSuccessResponse("content", data);
    } catch (Exception e) {
      logger.error("Exception fetching details for TV ID: {}", id, e);
      return createErrorResponse("Internal Server Error");
    }
  }

  @GetMapping("/{id}/similar")
  public ResponseEntity<Map<String, Object>> getSimilarTv(@Nonnull @PathVariable String id) {
    try {
      String url = String.format("%s%s/similar?language=en-US&page=1", TMDB_TV_BASE, id);
      Map<String, Object> data = tmdbService.fetchFromTmdb(url);

      if (data == null) {
        return ResponseEntity.notFound().build();
      }

      return createSuccessResponse("similar", data.get("results"));
    } catch (Exception e) {
      logger.error("Exception fetching similar TV shows for ID: {}", id, e);
      return createErrorResponse("Internal Server Error");
    }
  }

  @GetMapping("/{category}")
  public ResponseEntity<Map<String, Object>> getTvByCategory(@Nonnull @PathVariable String category) {
    try {
      String url = String.format("%s%s?language=en-US&page=1", TMDB_TV_BASE, category);
      Map<String, Object> data = tmdbService.fetchFromTmdb(url);

      if (data == null) {
        return ResponseEntity.notFound().build();
      }

      return createSuccessResponse("content", data.get("results"));
    } catch (Exception e) {
      logger.error("Exception fetching TV shows for category: {}", category, e);
      return createErrorResponse("Internal Server Error");
    }
  }

  // Response standardization helpers
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