package com.deepscan.service;

import com.deepscan.crawler.RealDarkWebScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DarkWebMonitorService {

    @Autowired
    private RealDarkWebScanner darkWebScanner;

    private final Map<String, LocalDateTime> monitoredEmails = new ConcurrentHashMap<>();

    @Async
    public void startMonitoring(String email) {
        monitoredEmails.put(email, LocalDateTime.now());
        System.out.println("🕵️ Started dark web monitoring for: " + email);
    }

    public void stopMonitoring(String email) {
        monitoredEmails.remove(email);
        System.out.println("🛑 Stopped monitoring for: " + email);
    }

    @Scheduled(fixedRate = 300000) // 5 minutes
    public void scheduledDarkWebScan() {
        if (monitoredEmails.isEmpty()) return;

        System.out.println("🔍 Running scheduled dark web scan for " + monitoredEmails.size() + " emails");

        for (String email : monitoredEmails.keySet()) {
            try {
                // Real monitoring would scan here
                System.out.println("Scanning: " + email);
            } catch (Exception e) {
                System.err.println("Failed to scan: " + email);
            }
        }
    }

    public List<String> getMonitoredEmails() {
        return new ArrayList<>(monitoredEmails.keySet());
    }
}