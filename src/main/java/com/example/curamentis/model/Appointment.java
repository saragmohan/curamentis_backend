package com.example.curamentis.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;

@Data
@Document(collection = "appointments")
public class Appointment {
    @Id
    private String id;

    @NotBlank
    private String name;

    @NotBlank
    private String mobile;

    @NotBlank
    private String dateTime;
    
    private String date;
    private String time;
}
