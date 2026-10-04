package com.example.curamentis.controller;

import com.example.curamentis.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private EmailService emailService;

    @GetMapping
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        return ResponseEntity.ok(status);
    }

    @GetMapping("/test-email")
    public ResponseEntity<Map<String, Object>> testEmail() {
        Map<String, Object> response = emailService.sendTestEmail();
        return ResponseEntity.ok(response);
    }
}

