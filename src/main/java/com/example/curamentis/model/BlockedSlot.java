package com.example.curamentis.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;

@Data
@Document(collection = "blocked_slots")
public class BlockedSlot {
    @Id
    private String id;

    @NotBlank
    private String date; // YYYY-MM-DD format

    @NotBlank
    private String time; // HH:mm format
}
