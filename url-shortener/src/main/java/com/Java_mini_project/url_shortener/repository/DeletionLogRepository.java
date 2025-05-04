package com.Java_mini_project.url_shortener.repository;

import com.Java_mini_project.url_shortener.model.DeletionLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeletionLogRepository extends MongoRepository<DeletionLog, String> {
}
