package com.netflix.backend.controller;

import com.netflix.backend.service.IngestionService;
import com.netflix.backend.service.TmdbService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stream")
@RequiredArgsConstructor
public class VideoController {

    private final TmdbService tmdbService;
    private final IngestionService ingestionService;
    private final Path videoStorageLocation = Paths.get("storage/hls-content");

    @GetMapping("/{type}/{id}")
    public Map<String, Object> getStreamUrl(@PathVariable String type, @PathVariable String id, Authentication auth) {
        Map<String, Object> response = new HashMap<>();

        // 1. Check if we have a custom transcoded HLS version locally
        Path masterManifestPath = videoStorageLocation.resolve(id).resolve("master.m3u8");

        if (Files.exists(masterManifestPath)) {
            // Serve our local high-quality ABR stream
            String streamToken = generateStreamToken(auth != null ? auth.getName() : "anonymous");
            response.put("url", "/api/v1/videos/" + id + "/master.m3u8?token=" + streamToken);
            response.put("provider", "Nebula Local Storage (ABR Active)");
            response.put("isLocal", true);
            response.put("status", "READY");
        } else {
            // 2. Integration with TMDB Videos -> Trigger Ingestion
            String youtubeKey = tmdbService.extractYoutubeKey(type, id);
            String jobStatus = ingestionService.getStatus(id);

            if (youtubeKey != null && "NOT_STARTED".equals(jobStatus)) {
                // Trigger background download and transcode
                ingestionService.ingestFromYoutube(id, youtubeKey);
                response.put("status", "INGESTION_STARTED");
            } else {
                response.put("status", jobStatus);
            }

            // While processing, we provide a high-quality HLS demo stream
            String demoHls = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8";

            response.put("url", demoHls);
            response.put("provider", "Nebula Global CDN (Auto-Resolution Active)");
            response.put("tmdb_id", id);
            response.put("isLocal", false);
            response.put("youtubeKey", youtubeKey);
        }

        response.put("protocol", "HLS");
        return response;
    }

    private String generateStreamToken(String username) {
        return UUID.randomUUID().toString();
    }
}
