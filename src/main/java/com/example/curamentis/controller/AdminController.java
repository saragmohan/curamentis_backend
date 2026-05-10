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

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private BlockedSlotRepository blockedSlotRepository;

    @Autowired
    private TimeSlotConfigRepository timeSlotConfigRepository;

    @GetMapping("/appointments")
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        return ResponseEntity.ok(appointmentRepository.findAll());
    }

    @DeleteMapping("/appointments/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable String id) {
        if (appointmentRepository.existsById(id)) {
            appointmentRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("success", true));
        }
        return ResponseEntity.notFound().build();
    }

    // --- Blocked Slots APIs ---

    @GetMapping("/blocked-slots")
    public ResponseEntity<List<BlockedSlot>> getBlockedSlots() {
        return ResponseEntity.ok(blockedSlotRepository.findAll());
    }

    @PostMapping("/blocked-slots")
    public ResponseEntity<BlockedSlot> addBlockedSlot(@RequestBody Map<String, String> payload) {
        String date = payload.get("date");
        String time = payload.get("time");
        
        Optional<BlockedSlot> existing = blockedSlotRepository.findByDateAndTime(date, time);
        if (existing.isPresent()) {
            return ResponseEntity.ok(existing.get()); // Already blocked
        }

        BlockedSlot slot = new BlockedSlot();
        slot.setDate(date);
        slot.setTime(time);
        return ResponseEntity.ok(blockedSlotRepository.save(slot));
    }

    @DeleteMapping("/blocked-slots")
    public ResponseEntity<?> unblockSlot(@RequestBody Map<String, String> payload) {
        String date = payload.get("date");
        String time = payload.get("time");
        
        Optional<BlockedSlot> existing = blockedSlotRepository.findByDateAndTime(date, time);
        existing.ifPresent(blockedSlot -> blockedSlotRepository.delete(blockedSlot));
        
        return ResponseEntity.ok(Map.of("success", true));
    }

    // --- Time Slot Config APIs ---

    @GetMapping("/time-slots")
    public ResponseEntity<List<TimeSlotConfig>> getAllTimeSlots() {
        return ResponseEntity.ok(timeSlotConfigRepository.findAll());
    }

    @PostMapping("/time-slots")
    public ResponseEntity<TimeSlotConfig> updateTimeSlots(@RequestBody TimeSlotConfig payload) {
        Optional<TimeSlotConfig> existing = timeSlotConfigRepository.findByDate(payload.getDate());
        
        if (existing.isPresent()) {
            TimeSlotConfig config = existing.get();
            config.setAvailableTimes(payload.getAvailableTimes());
            return ResponseEntity.ok(timeSlotConfigRepository.save(config));
        } else {
            return ResponseEntity.ok(timeSlotConfigRepository.save(payload));
        }
    }

    @DeleteMapping("/time-slots/{date}")
    public ResponseEntity<?> deleteTimeSlots(@PathVariable String date) {
        Optional<TimeSlotConfig> existing = timeSlotConfigRepository.findByDate(date);
        existing.ifPresent(config -> timeSlotConfigRepository.delete(config));
        return ResponseEntity.ok(Map.of("success", true));
    }
}
