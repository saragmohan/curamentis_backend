package com.example.curamentis.repository;

import com.example.curamentis.model.BlockedSlot;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface BlockedSlotRepository extends MongoRepository<BlockedSlot, String> {
    List<BlockedSlot> findByDate(String date);
    Optional<BlockedSlot> findByDateAndTime(String date, String time);
}
