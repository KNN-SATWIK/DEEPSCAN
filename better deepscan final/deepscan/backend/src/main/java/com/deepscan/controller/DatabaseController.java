package com.deepscan.controller;

import com.deepscan.model.BreachRecord;
import com.deepscan.repository.BreachRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/db")
@CrossOrigin(origins = "*")
public class DatabaseController {

    @Autowired
    private BreachRepository breachRepository;

    // Get all breach records
    @GetMapping("/breaches")
    public List<BreachRecord> getAllBreaches() {
        return breachRepository.findAll();
    }

    // Get breach count
    @GetMapping("/breaches/count")
    public Map<String, Object> getBreachCount() {
        long count = breachRepository.count();
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        response.put("database", "mysql");
        response.put("message", "Total breach records in MySQL: " + count);
        return response;
    }

    // Add a new breach record
    @PostMapping("/breaches")
    public BreachRecord addBreach(@RequestBody BreachRecord breach) {
        return breachRepository.save(breach);
    }

    // Update a breach record
    @PutMapping("/breaches/{id}")
    public BreachRecord updateBreach(@PathVariable Long id, @RequestBody BreachRecord breachDetails) {
        BreachRecord breach = breachRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Breach record not found"));

        breach.setEmail(breachDetails.getEmail());
        breach.setPasswordHash(breachDetails.getPasswordHash());
        breach.setBreachSource(breachDetails.getBreachSource());
        breach.setBreachDate(breachDetails.getBreachDate());

        return breachRepository.save(breach);
    }

    // Delete a breach record
    @DeleteMapping("/breaches/{id}")
    public String deleteBreach(@PathVariable Long id) {
        breachRepository.deleteById(id);
        return "Breach record deleted successfully";
    }

    // Clear all breaches
    @DeleteMapping("/breaches")
    public String clearAllBreaches() {
        breachRepository.deleteAll();
        return "All breach records cleared from MySQL database";
    }

    // Get database info
    @GetMapping("/info")
    public Map<String, String> getDatabaseInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("database", "mysql");
        info.put("status", "Connected");
        info.put("records", String.valueOf(breachRepository.count()));
        info.put("type", "MySQL Server");
        info.put("message", "Using MySQL database server");

        return info;
    }

    // Search breaches by email
    @GetMapping("/breaches/search")
    public List<BreachRecord> searchBreaches(@RequestParam String email) {
        return breachRepository.findAll().stream()
                .filter(breach -> breach.getEmail().toLowerCase().contains(email.toLowerCase()))
                .toList();
    }
}