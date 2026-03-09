package com.netflix.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for interacting with the The Movie Database (TMDB) API.
 * Follows Google Java Style: 2-space indentation and explicit HTTP handling.
 */
@Service
public class TmdbService {

  private static final Logger logger = LoggerFactory.getLogger(TmdbService.class);
  private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/";

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final String tmdbApiKey;

  // Constructor-based injection for API key and initialization
  public TmdbService(@Value("${tmdb.api.key}") String tmdbApiKey) {
    this.tmdbApiKey = tmdbApiKey;
    this.httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .build();
    this.objectMapper = new ObjectMapper();
  }

  /**
   * Fetches data from TMDB and parses it into a Map.
   */
  @Nullable
  public Map<String, Object> fetchFromTmdb(@Nonnull String relativeUrl) {
    try {
      String finalUrl = buildUrl(relativeUrl);
      HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
          .header("accept", "application/json");

      // Handle both API Key (v3) and Bearer Token (v4) authentication
      if (tmdbApiKey != null && tmdbApiKey.length() <= 32) {
        String separator = finalUrl.contains("?") ? "&" : "?";
        finalUrl = finalUrl + separator + "api_key=" + tmdbApiKey;
      } else if (tmdbApiKey != null) {
        requestBuilder.header("Authorization", "Bearer " + tmdbApiKey);
      }

      HttpRequest request = requestBuilder
          .uri(URI.create(finalUrl))
          .GET()
          .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 404) {
        logger.warn("TMDB resource not found: {}", relativeUrl);
        return null;
      }

      if (response.statusCode() != 200) {
        logger.error("TMDB API error: Status {}. Body: {}", response.statusCode(), response.body());
        throw new RuntimeException("Failed to fetch data from TMDB");
      }

      return objectMapper.readValue(response.body(), Map.class);

    } catch (Exception e) {
      logger.error("Critical error during TMDB fetch for URL: {}", relativeUrl, e);
      throw new RuntimeException("TMDB service error", e);
    }
  }

  /**
   * Extracts a YouTube key for a given movie/TV ID to trigger ingestion.
   */
  @Nullable
  @SuppressWarnings("unchecked")
  public String extractYoutubeKey(@Nonnull String type, @Nonnull String id) {
    try {
      String relativeUrl = type + "/" + id + "/videos?language=en-US";
      Map<String, Object> data = fetchFromTmdb(relativeUrl);

      if (data == null || !data.containsKey("results")) {
        return null;
      }

      List<Map<String, Object>> results = (List<Map<String, Object>>) data.get("results");
      for (Map<String, Object> video : results) {
        String site = (String) video.get("site");
        String videoType = (String) video.get("type");

        if ("YouTube".equalsIgnoreCase(site) && 
            ("Trailer".equalsIgnoreCase(videoType) || "Teaser".equalsIgnoreCase(videoType))) {
          return (String) video.get("key");
        }
      }
    } catch (Exception e) {
      logger.error("Failed to extract YouTube key for {} ID: {}", type, id, e);
    }
    return null;
  }

  private String buildUrl(String relativeUrl) {
    if (relativeUrl.startsWith("http")) {
      return relativeUrl;
    }
    return TMDB_BASE_URL + relativeUrl;
  }
}