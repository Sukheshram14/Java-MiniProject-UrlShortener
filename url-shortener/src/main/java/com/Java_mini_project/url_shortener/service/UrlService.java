package com.Java_mini_project.url_shortener.service;

import com.Java_mini_project.url_shortener.model.Url;
import com.Java_mini_project.url_shortener.model.DeletionLog;
import com.Java_mini_project.url_shortener.repository.UrlRepository;
import com.Java_mini_project.url_shortener.repository.DeletionLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class UrlService {
    private static final Logger logger = LoggerFactory.getLogger(UrlService.class);
    private final UrlRepository urlRepository;
    private final DeletionLogRepository deletionLogRepository;

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SHORT_URL_LENGTH = 6;

    public UrlService(UrlRepository urlRepository, DeletionLogRepository deletionLogRepository) {
        this.urlRepository = urlRepository;
        this.deletionLogRepository = deletionLogRepository;
    }

    
    public String generateShortUrl() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(SHORT_URL_LENGTH);
        for (int i = 0; i < SHORT_URL_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    // ✅ Saves a new URL with optional custom alias & domain
    public Url saveUrl(String originalUrl, String customAlias, String customDomain, Instant expiresAt) {
        String shortUrl = (customAlias != null && !customAlias.isEmpty()) ? customAlias : generateShortUrl();

        // Ensure the domain is included in the short URL, otherwise it will be incorrect.
        if (customDomain == null || customDomain.isEmpty()) {
            customDomain = "http://localhost:8080"; // Default domain for local dev or change to your custom domain
        }

        if (urlRepository.findByShortUrl(shortUrl).isPresent()) {
            throw new RuntimeException("Custom alias already taken!");
        }

        // Default expiration is set to 7 days if not provided
        if (expiresAt == null) {
            expiresAt = Instant.now().plusSeconds(7 * 24 * 60 * 60);
        }

        // Create the URL object with all data, including the domain
        Url url = new Url();
        url.setShortUrl(shortUrl);
        url.setOriginalUrl(originalUrl);
        url.setCreatedAt(Instant.now());
        url.setExpiresAt(expiresAt);
        url.setClickCount(0);
        url.setCustomDomain(customDomain);

        return urlRepository.save(url);
    }

    // ✅ Retrieves a URL by short code
    public Optional<Url> getUrl(String shortUrl) {
        Optional<Url> urlOptional = urlRepository.findByShortUrl(shortUrl);

        if (urlOptional.isPresent()) {
            Url url = urlOptional.get();

            if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(Instant.now())) {
                logger.warn("Short URL '{}' expired at {}", shortUrl, url.getExpiresAt());

                // Save deletion log
                DeletionLog log = new DeletionLog();
                log.setShortUrl(url.getShortUrl());
                log.setOriginalUrl(url.getOriginalUrl());
                log.setExpiredAt(url.getExpiresAt());
                log.setDeletedAt(Instant.now());
                deletionLogRepository.save(log);

                // Delete expired URL from database
                urlRepository.delete(url);

                return Optional.empty();
            }
            return Optional.of(url);
        }

        return Optional.empty();
    }

    // ✅ Increments click count
    public void incrementClickCount(Url url) {
        url.setClickCount(url.getClickCount() + 1);
        urlRepository.save(url);
    }

    // ✅ Scheduled task: Deletes expired links every hour
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void deleteExpiredLinks() {
        List<Url> expiredUrls = urlRepository.findAll().stream()
            .filter(url -> url.getExpiresAt() != null && url.getExpiresAt().isBefore(Instant.now()))
            .toList();

        for (Url url : expiredUrls) {
            logger.warn("Deleting expired short URL: {}", url.getShortUrl());
            urlRepository.delete(url);
        }
    }

    // ✅ Calculates the time remaining before expiration
    public long getTimeRemainingBeforeExpiration(String shortUrl) {
        Optional<Url> urlOptional = urlRepository.findByShortUrl(shortUrl);
        if (urlOptional.isPresent()) {
            Url url = urlOptional.get();
            Instant expiresAt = url.getExpiresAt();
            
            if (expiresAt != null) {
                long timeRemaining = expiresAt.toEpochMilli() - Instant.now().toEpochMilli();
                return timeRemaining > 0 ? timeRemaining : 0;  // If expired, return 0
            }
        }
        return 0;  // Return 0 if the URL is not found or has no expiration
    }
}
