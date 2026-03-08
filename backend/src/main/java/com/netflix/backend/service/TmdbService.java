package com.netflix.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.List;

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

            if (tmdbApiKey != null && tmdbApiKey.length() <= 32) {
                String separator = url.contains("?") ? "&" : "?";
                url = url + separator + "api_key=" + tmdbApiKey;
            } else if (tmdbApiKey != null) {
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
                throw new RuntimeException("Failed to fetch data from TMDB: " + response.statusCode());
            }

            return objectMapper.readValue(response.body(), Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching from TMDB", e);
        }
    }

    @SuppressWarnings("unchecked")
    public String extractYoutubeKey(String type, String id) {
        try {
            Map<String, Object> data = fetchFromTmdb(
                    "https://api.themoviedb.org/3/" + type + "/" + id + "/videos?language=en-US");
            if (data == null || !data.containsKey("results"))
                return null;

            List<Map<String, Object>> results = (List<Map<String, Object>>) data.get("results");
            for (Map<String, Object> video : results) {
                String site = (String) video.get("site");
                String videoType = (String) video.get("type");
                if ("YouTube".equalsIgnoreCase(site)
                        && ("Trailer".equalsIgnoreCase(videoType) || "Teaser".equalsIgnoreCase(videoType))) {
                    return (String) video.get("key");
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
