package com.example.curamentis.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Data
@Document(collection = "time_slot_configs")
public class TimeSlotConfig {
    @Id
    private String id;

    @NotBlank
    private String date; // YYYY-MM-DD format

    private List<String> availableTimes; // List of HH:mm format times
}
