package com.Java_mini_project.url_shortener.repository;

import com.Java_mini_project.url_shortener.model.Url;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UrlRepository extends MongoRepository<Url, String> {
    Optional<Url> findByShortUrl(String shortUrl);
}
