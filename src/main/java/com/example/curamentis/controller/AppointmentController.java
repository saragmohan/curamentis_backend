package com.example.curamentis.controller;

import com.example.curamentis.model.Appointment;
import com.example.curamentis.model.BlockedSlot;
import com.example.curamentis.model.TimeSlotConfig;
import com.example.curamentis.repository.AppointmentRepository;
import com.example.curamentis.repository.BlockedSlotRepository;
import com.example.curamentis.repository.TimeSlotConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private BlockedSlotRepository blockedSlotRepository;

    @Autowired
    private TimeSlotConfigRepository timeSlotConfigRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAppointments() {
        List<Appointment> appointments = appointmentRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("appointments", appointments);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> saveAppointment(@Valid @RequestBody Appointment appointment) {
        // Parse dateTime to extract date and time if not provided separately
        if (appointment.getDateTime() != null && appointment.getDate() == null) {
            String[] parts = appointment.getDateTime().split(" ");
            if (parts.length >= 2) {
                appointment.setDate(parts[0]);
                appointment.setTime(parts[1]);
            }
        }
        
        Appointment saved = appointmentRepository.save(appointment);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("appointment", saved);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/time-slots")
    public ResponseEntity<Map<String, Object>> getTimeSlots() {
        List<TimeSlotConfig> configs = timeSlotConfigRepository.findAll();
        Map<String, List<String>> timeSlotsMap = new HashMap<>();
        for (TimeSlotConfig config : configs) {
            timeSlotsMap.put(config.getDate(), config.getAvailableTimes());
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("timeSlots", timeSlotsMap);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/blocked-slots")
    public ResponseEntity<List<BlockedSlot>> getBlockedSlots() {
        return ResponseEntity.ok(blockedSlotRepository.findAll());
    }
}
