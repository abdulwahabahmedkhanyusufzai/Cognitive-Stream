package com.netflix.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main entry point for the Netflix Backend service.
 * Enables asynchronous processing for video ingestion and TMDB metadata fetching.
 * Follows Google Java Style: 2-space indentation and explicit documentation.
 */
@SpringBootApplication
@EnableAsync // Required for IngestionService.ingestFromYoutube() to run in background
public class BackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(BackendApplication.class, args);
  }
}