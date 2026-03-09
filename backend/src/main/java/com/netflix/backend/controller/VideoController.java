package com.netflix.backend.controller;

import com.netflix.backend.service.IngestionService;
import com.netflix.backend.service.TmdbService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsible for resolving video stream URLs and triggering ingestion.
 * Follows Google Java Style: 2-space indentation and defensive path validation.
 */
@RestController
@RequestMapping("/api/v1/stream")
public class VideoController {

  private static final Logger logger = LoggerFactory.getLogger(VideoController.class);
  private static final String DEMO_HLS_URL = 
      "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8";
  
  private final TmdbService tmdbService;
  private final IngestionService ingestionService;
  private final Path videoStorageLocation;

  // Explicit constructor for maximum traceability in large monorepos
  public VideoController(
      @Nonnull TmdbService tmdbService,
      @Nonnull IngestionService ingestionService,
      @Value("${app.video.storage-path:storage/hls-content}") String storagePath) {
    this.tmdbService = tmdbService;
    this.ingestionService = ingestionService;
    this.videoStorageLocation = Paths.get(storagePath).toAbsolutePath().normalize();
  }

  @GetMapping("/{type}/{id}")
  public Map<String, Object> getStreamUrl(
      @Nonnull @PathVariable String type, 
      @Nonnull @PathVariable String id, 
      Authentication auth) {
    
    Map<String, Object> response = new HashMap<>();
    String username = (auth != null) ? auth.getName() : "anonymous";

    // 1. Security check: Validate and normalize the path to prevent escape
    Path contentDir = videoStorageLocation.resolve(id).normalize();
    if (!contentDir.startsWith(videoStorageLocation)) {
      logger.warn("Security: Blocked illegal path access attempt for ID: {}", id);
      response.put("status", "ERROR");
      response.put("message", "Invalid Content ID");
      return response;
    }

    Path masterManifestPath = contentDir.resolve("master.m3u8");

    // 2. Resolve Stream Logic
    if (Files.exists(masterManifestPath)) {
      handleLocalStream(response, id, username);
    } else {
      handleIngestionFlow(response, type, id);
    }

    response.put("protocol", "HLS");
    return response;
  }

  private void handleLocalStream(Map<String, Object> response, String id, String username) {
    String streamToken = UUID.randomUUID().toString();
    logger.info("Serving local ABR stream for ID: {} to user: {}", id, username);
    
    response.put("url", String.format("/api/v1/videos/%s/master.m3u8?token=%s", id, streamToken));
    response.put("provider", "Nebula Local Storage (ABR Active)");
    response.put("isLocal", true);
    response.put("status", "READY");
  }

  private void handleIngestionFlow(Map<String, Object> response, String type, String id) {
    String youtubeKey = tmdbService.extractYoutubeKey(type, id);
    String jobStatus = ingestionService.getStatus(id);

    if (youtubeKey != null && "NOT_STARTED".equals(jobStatus)) {
      logger.info("Triggering background ingestion for TMDB ID: {} (YouTube: {})", id, youtubeKey);
      ingestionService.ingestFromYoutube(id, youtubeKey);
      response.put("status", "INGESTION_STARTED");
    } else {
      response.put("status", jobStatus);
    }

    // Fallback to CDN demo while transcoding occurs
    response.put("url", DEMO_HLS_URL);
    response.put("provider", "Nebula Global CDN (Auto-Resolution Active)");
    response.put("tmdb_id", id);
    response.put("isLocal", false);
    response.put("youtubeKey", youtubeKey);
  }
}