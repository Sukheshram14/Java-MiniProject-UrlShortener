package com.Java_mini_project.url_shortener.controller;

import com.Java_mini_project.url_shortener.model.UrlRequest;
import com.Java_mini_project.url_shortener.model.Url;
import com.Java_mini_project.url_shortener.service.UrlService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000") // Allow only requests from React app
public class UrlController {
    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/shorten")
    public ResponseEntity<?> shortenUrl(@RequestBody UrlRequest request) {
        Url shortenedUrl = urlService.saveUrl(
            request.getOriginalUrl(),
            request.getCustomAlias(),
            request.getCustomDomain(), // Ensure this is passed
            request.getExpiresAt()
        );

        return ResponseEntity.ok(shortenedUrl);
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<?> redirect(@PathVariable String shortUrl) {
        Optional<Url> urlOptional = urlService.getUrl(shortUrl);

        if (urlOptional.isPresent()) {
            Url url = urlOptional.get();

            // Expiration Check
            if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(Instant.now())) {
                return ResponseEntity.status(410).body("This link has expired!"); // 410 Gone
            }

            urlService.incrementClickCount(url);
            return ResponseEntity.status(302).location(URI.create(url.getOriginalUrl())).build();
        }

        return ResponseEntity.notFound().build();
    }

    // New endpoint to check the remaining time before expiration
    @GetMapping("/remaining-time/{shortUrl}")
    public ResponseEntity<?> getRemainingTime(@PathVariable String shortUrl) {
        long remainingTime = urlService.getTimeRemainingBeforeExpiration(shortUrl);
        if (remainingTime > 0) {
            return ResponseEntity.ok("Time remaining before expiration: " + remainingTime + " milliseconds");
        } else {
            return ResponseEntity.status(410).body("The link has expired or does not exist.");
        }
    }
}
