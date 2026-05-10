package com.example.curamentis.repository;

import com.example.curamentis.model.TimeSlotConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface TimeSlotConfigRepository extends MongoRepository<TimeSlotConfig, String> {
    Optional<TimeSlotConfig> findByDate(String date);
}
