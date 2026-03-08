package com.netflix.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TmdbService {

    @Value("${tmdb.api.key}")
    private String tmdbApiKey;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TmdbService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> fetchFromTmdb(String baseUrl) {
        try {
            String url = baseUrl;
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .header("accept", "application/json");

            // Check if we are using a v3 API Key (32 chars) or a v4 Read Access Token (long
            // JWT)
            if (tmdbApiKey != null && tmdbApiKey.length() <= 32) {
                // v3 API Key style: append as query parameter
                String separator = url.contains("?") ? "&" : "?";
                url = url + separator + "api_key=" + tmdbApiKey;
            } else if (tmdbApiKey != null) {
                // v4 Read Access Token style: use Authorization header
                requestBuilder.header("Authorization", "Bearer " + tmdbApiKey);
            }

            HttpRequest request = requestBuilder
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                if (response.statusCode() == 404) {
                    return null;
                }
                // Log the fail but hide the key
                throw new RuntimeException("Failed to fetch data from TMDB: " + response.statusCode() + " for URL: "
                        + baseUrl.replaceAll("api_key=[^&]+", "api_key=***"));
            }

            return objectMapper.readValue(response.body(), Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching from TMDB", e);
        }
    }
}
