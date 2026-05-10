package com.example.curamentis.controller;

import com.example.curamentis.model.ContactMessage;
import com.example.curamentis.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    @Autowired
    private ContactRepository contactRepository;

    @PostMapping
    public ResponseEntity<Map<String, Object>> submitContact(@Valid @RequestBody ContactMessage message) {
        ContactMessage saved = contactRepository.save(message);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Contact submitted successfully");
        return ResponseEntity.ok(response);
    }
}
