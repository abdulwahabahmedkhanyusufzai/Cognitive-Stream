package com.netflix.backend.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service responsible for orchestrating video ingestion and transcoding workflows.
 * Follows Google Java Style: 2-space indentation and explicit process management.
 */
@Service
public class IngestionService {

  private static final Logger logger = LoggerFactory.getLogger(IngestionService.class);
  private static final String SCRIPT_REL_PATH = "scripts/ingest_and_transcode.ps1";

  private final Map<String, String> jobStatus = new ConcurrentHashMap<>();
  private final Path storagePath;

  // Explicit constructor for maximum traceability
  public IngestionService(@Value("${app.video.storage-path:storage/hls-content}") String storage) {
    this.storagePath = Paths.get(storage).toAbsolutePath().normalize();
  }

  /**
   * Asynchronously triggers the PowerShell script for YouTube download and transcoding.
   */
  @Async
  public void ingestFromYoutube(@Nonnull String videoId, @Nonnull String youtubeKey) {
    if (jobStatus.containsKey(videoId)) {
      return;
    }

    jobStatus.put(videoId, "PROCESSING");
    logger.info("Starting ingestion for video ID: {} (YouTube Key: {})", videoId, youtubeKey);

    try {
      // Security: Normalize and validate the output directory
      Path outputDir = storagePath.resolve(videoId).normalize();
      if (!outputDir.startsWith(storagePath)) {
        logger.error("Security violation: Blocked ingestion attempt for illegal path: {}", outputDir);
        jobStatus.put(videoId, "ERROR");
        return;
      }

      Files.createDirectories(outputDir);

      String scriptPath = Paths.get(SCRIPT_REL_PATH).toAbsolutePath().toString();
      executeIngestionProcess(videoId, youtubeKey, outputDir, scriptPath);

    } catch (Exception e) {
      jobStatus.put(videoId, "ERROR");
      logger.error("Critical error during ingestion for video ID: {}", videoId, e);
    }
  }

  private void executeIngestionProcess(
      String videoId, String youtubeKey, Path outputDir, String scriptPath) throws Exception {

    ProcessBuilder pb = new ProcessBuilder(
        "powershell.exe", "-ExecutionPolicy", "Bypass", "-File", scriptPath,
        "-YoutubeKey", youtubeKey,
        "-OutputDir", outputDir.toAbsolutePath().toString());

    pb.redirectErrorStream(true);
    Process process = pb.start();

    // Stream consumption ensures the process doesn't hang due to a full buffer
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        logger.debug("[Ingestion-{}]: {}", videoId, line);
      }
    }

    int exitCode = process.waitFor();
    if (exitCode == 0) {
      jobStatus.put(videoId, "COMPLETED");
      logger.info("Ingestion completed successfully for video ID: {}", videoId);
    } else {
      jobStatus.put(videoId, "FAILED");
      logger.error("Ingestion failed for video ID: {} with exit code: {}", videoId, exitCode);
    }
  }

  @Nonnull
  public String getStatus(@Nonnull String videoId) {
    return jobStatus.getOrDefault(videoId, "NOT_STARTED");
  }
}