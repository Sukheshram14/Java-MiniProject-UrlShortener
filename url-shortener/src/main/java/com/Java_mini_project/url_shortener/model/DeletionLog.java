package com.Java_mini_project.url_shortener.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data
@Document(collection = "deletion_logs")
public class DeletionLog {
    @Id
    private String id;
    private String shortUrl;
    private String originalUrl;
    private Instant expiredAt;
    private Instant deletedAt;
}
