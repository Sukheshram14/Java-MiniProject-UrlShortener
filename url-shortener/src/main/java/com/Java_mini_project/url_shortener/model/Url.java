package com.Java_mini_project.url_shortener.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "urls")
public class Url {
    @Id
    private String shortUrl;

    private String originalUrl;
    private Instant createdAt;

    @Indexed(expireAfterSeconds = 0)  // ✅ MongoDB TTL Index: Auto-delete when expiresAt is reached
    private Instant expiresAt;

    private long clickCount;
    private String customDomain; // ✅ New field for branded short links
}
