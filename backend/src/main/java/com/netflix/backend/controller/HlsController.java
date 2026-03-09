package com.netflix.backend.controller;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for serving HTTP Live Streaming (HLS) content.
 * Follows Google Java Style: 2-space indentation and explicit path validation.
 */
@RestController
@RequestMapping("/api/v1/videos")
public class HlsController {

  private static final Logger logger = LoggerFactory.getLogger(HlsController.class);
  private final Path videoStorageLocation;

  public HlsController(@Value("${app.video.storage-path:storage/hls-content}") String storagePath) {
    this.videoStorageLocation = Paths.get(storagePath).toAbsolutePath().normalize();
  }

  @GetMapping("/{videoName}/{fileName:.+}")
  public ResponseEntity<Resource> serveHlsFiles(
      @Nonnull @PathVariable String videoName,
      @Nonnull @PathVariable String fileName) {

    try {
      // Security: Normalize and validate the path to prevent Path Traversal attacks
      Path filePath = videoStorageLocation.resolve(videoName).resolve(fileName).normalize();
      
      if (!filePath.startsWith(videoStorageLocation)) {
        logger.warn("Potential Path Traversal attempt blocked: {}", filePath);
        return ResponseEntity.status(403).build();
      }

      Resource resource = new UrlResource(filePath.toUri());

      if (resource.exists() && resource.isReadable()) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, determineMimeType(fileName))
            .body(resource);
      }
      
      return ResponseEntity.notFound().build();

    } catch (MalformedURLException e) {
      logger.error("Error generating URL resource for file: {}", fileName, e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Helper to map file extensions to correct HLS MIME types.
   */
  private String determineMimeType(@Nonnull String fileName) {
    if (fileName.endsWith(".m3u8")) {
      return "application/vnd.apple.mpegurl";
    } else if (fileName.endsWith(".ts")) {
      return "video/MP2T";
    }
    return MediaType.APPLICATION_OCTET_STREAM_VALUE;
  }
}