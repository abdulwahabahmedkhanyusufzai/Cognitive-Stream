package com.netflix.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngestionService {

    private final Map<String, String> jobStatus = new ConcurrentHashMap<>();
    private final Path storagePath = Paths.get("storage/hls-content");

    @Async
    public void ingestFromYoutube(String videoId, String youtubeKey) {
        if (jobStatus.containsKey(videoId))
            return;

        jobStatus.put(videoId, "PROCESSING");
        log.info("Starting ingestion for video ID: {} (YouTube Key: {})", videoId, youtubeKey);

        try {
            Path outputDir = storagePath.resolve(videoId);
            Files.createDirectories(outputDir);

            // Path to our orchestration script
            String scriptPath = Paths.get("scripts/ingest_and_transcode.ps1").toAbsolutePath().toString();

            ProcessBuilder pb = new ProcessBuilder(
                    "powershell.exe", "-ExecutionPolicy", "Bypass", "-File", scriptPath,
                    "-YoutubeKey", youtubeKey,
                    "-OutputDir", outputDir.toAbsolutePath().toString());

            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("[Ingestion Script] {}", line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                jobStatus.put(videoId, "COMPLETED");
                log.info("Ingestion completed successfully for video ID: {}", videoId);
            } else {
                jobStatus.put(videoId, "FAILED");
                log.error("Ingestion failed for video ID: {} with exit code: {}", videoId, exitCode);
            }

        } catch (Exception e) {
            jobStatus.put(videoId, "ERROR");
            log.error("Error during ingestion for video ID: {}", videoId, e);
        }
    }

    public String getStatus(String videoId) {
        return jobStatus.getOrDefault(videoId, "NOT_STARTED");
    }
}
