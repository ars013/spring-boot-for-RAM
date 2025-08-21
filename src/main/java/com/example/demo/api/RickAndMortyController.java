package com.example.demo.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;

import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class RickAndMortyController {

    private final RestTemplate restTemplate;

    @Value("${ram.baseUrl:https://rickandmortyapi.com/api}")
    private String baseUrl;

    public RickAndMortyController(RestTemplateBuilder builder) {
        // small timeouts so app doesn’t hang if the external API is slow
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .build();
    }

    @GetMapping("/character")
    public ResponseEntity<String> characters(@RequestParam String name) {
        return proxyGet("/character/", name);
    }

    @GetMapping("/episode")
    public ResponseEntity<String> episodes(@RequestParam String name) {
        return proxyGet("/episode/", name);
    }

    @GetMapping("/location")
    public ResponseEntity<String> locations(@RequestParam String name) {
        return proxyGet("/location/", name);
    }

    // --- helper: builds the external URL and proxies the JSON as-is ---
    private ResponseEntity<String> proxyGet(String resourcePath, String name) {
        URI uri = UriComponentsBuilder
                .fromUriString(baseUrl + resourcePath)
                .queryParam("name", name)
                .build(true)
                .toUri();
        try {
            String body = restTemplate.getForObject(uri, String.class);
            return ResponseEntity.ok(body);
        } catch (HttpClientErrorException.NotFound e) {
            // forward a 404 when the external API says “no results”
            return ResponseEntity.status(404).body(e.getResponseBodyAsString());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }
}

