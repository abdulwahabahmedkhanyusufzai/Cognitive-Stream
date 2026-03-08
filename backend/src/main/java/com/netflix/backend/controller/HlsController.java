package com.netflix.backend.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/v1/videos")
public class HlsController {

    private final Path videoStorageLocation = Paths.get("storage/hls-content");

    @GetMapping("/{videoName}/{fileName:.+}")
    public ResponseEntity<Resource> serveHlsFiles(
            @PathVariable String videoName,
            @PathVariable String fileName) {

        try {
            Path filePath = videoStorageLocation.resolve(videoName).resolve(fileName);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                String contentType = "application/octet-stream";
                if (fileName.endsWith(".m3u8")) {
                    contentType = "application/vnd.apple.mpegurl";
                } else if (fileName.endsWith(".ts")) {
                    contentType = "video/MP2T";
                }

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
