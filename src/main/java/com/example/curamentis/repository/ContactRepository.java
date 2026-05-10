package com.example.curamentis.repository;

import com.example.curamentis.model.ContactMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ContactRepository extends MongoRepository<ContactMessage, String> {
}
