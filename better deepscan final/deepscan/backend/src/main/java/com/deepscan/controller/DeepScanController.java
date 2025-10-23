package com.deepscan.controller;

import com.deepscan.model.*;
import com.deepscan.service.BreachService;
import com.deepscan.service.TorCrawlerService;
import com.deepscan.service.DarkWebMonitorService;
import com.deepscan.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class DeepScanController {

    @Autowired
    private BreachService breachService;

    @Autowired
    private TorCrawlerService torCrawlerService;

    @Autowired
    private DarkWebMonitorService darkWebMonitorService;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ===== EXISTING ENDPOINTS =====

    @PostMapping("/scan/credentials")
    public ScanResult scanCredentials(@RequestBody ScanRequest request) {
        return breachService.fullScan(request);
    }

    @PostMapping("/scan/deepweb")
    public CompletableFuture<DeepWebResult> scanDeepWeb(@RequestBody DeepWebScanRequest request) {
        return torCrawlerService.scanForCredentials(request);
    }

    @PostMapping("/monitor/start")
    public String startContinuousMonitoring(@RequestBody MonitorRequest request) {
        darkWebMonitorService.startMonitoring(request.getEmail());
        return "Continuous dark web monitoring started for: " + request.getEmail();
    }

    @PostMapping("/monitor/stop")
    public String stopMonitoring(@RequestBody MonitorRequest request) {
        darkWebMonitorService.stopMonitoring(request.getEmail());
        return "Monitoring stopped for: " + request.getEmail();
    }

    @GetMapping("/tor/status")
    public Map<String, Object> getTorStatus() {
        return torCrawlerService.getTorStatus();
    }

    @GetMapping("/stats")
    public SystemStats getSystemStats() {
        return breachService.getSystemStats();
    }

    @GetMapping("/health")
    public String healthCheck() {
        return "DEEPSCAN API - Cyberpunk Breach Intelligence - Status: ONLINE";
    }

    // ===== ANALYTICS ENDPOINTS =====

    @PostMapping("/analytics/add-credentials")
    public Map<String, Object> addCredentialsForAnalytics(@RequestBody AnalyticsRequest request) {
        Map<String, Object> response = new HashMap<>();

        System.out.println("=== ANALYTICS DEBUG ===");
        System.out.println("Received analytics request for: " + request.getEmail());

        try {
            AnalyticsRecord record = analyticsService.saveCredentials(
                    request.getEmail(),
                    request.getPassword()
            );

            long totalRecords = analyticsService.getTotalRecords();
            long passwordProvidedCount = analyticsService.getPasswordProvidedCount();

            response.put("status", "success");
            response.put("message", "Credentials successfully added to analytics database");
            response.put("recordId", record.getId());
            response.put("totalRecords", totalRecords);
            response.put("passwordProvidedRecords", passwordProvidedCount);
            response.put("timestamp", record.getCreatedAt());

            System.out.println("Analytics save successful. Total records: " + totalRecords);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error adding credentials: " + e.getMessage());
            response.put("errorDetails", e.toString());

            System.err.println("Analytics save failed: " + e.getMessage());
            e.printStackTrace();
        }

        return response;
    }

    // ===== DEBUG ENDPOINTS =====

    @GetMapping("/debug/db-test")
    public Map<String, Object> testDatabaseConnection() {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("=== DATABASE CONNECTION TEST ===");

            // Test if table exists
            Integer tableCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'deepscan' AND table_name = 'analytics_records'",
                    Integer.class
            );

            response.put("status", "success");
            response.put("tableExists", tableCount > 0);
            response.put("tableCount", tableCount);

            if (tableCount > 0) {
                // Count records
                Integer recordCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM analytics_records",
                        Integer.class
                );
                response.put("recordCount", recordCount);

                // Show recent records
                List<Map<String, Object>> recentRecords = jdbcTemplate.queryForList(
                        "SELECT * FROM analytics_records ORDER BY created_at DESC LIMIT 5"
                );
                response.put("recentRecords", recentRecords);
            }

            System.out.println("Database test completed: " + response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            System.err.println("Database test failed: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/debug/insert-test")
    public Map<String, Object> directInsertTest() {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("=== DIRECT INSERT TEST ===");

            // Direct SQL insert
            int affectedRows = jdbcTemplate.update(
                    "INSERT INTO analytics_records (email, password_text, password_provided, scan_type) VALUES (?, ?, ?, ?)",
                    "debug_test@example.com", "debugpassword123", 1, "debug_test"
            );

            response.put("status", "success");
            response.put("affectedRows", affectedRows);
            response.put("message", "Direct SQL insert completed");

            // Verify insertion
            Integer newCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM analytics_records WHERE email = 'debug_test@example.com'",
                    Integer.class
            );
            response.put("verifiedCount", newCount);

            System.out.println("Direct insert test completed: " + response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("sqlState", e.toString());
            System.err.println("Direct insert test failed: " + e.getMessage());
            e.printStackTrace();
        }
        return response;
    }

    @GetMapping("/debug/check-analytics-service")
    public Map<String, Object> checkAnalyticsService() {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("=== ANALYTICS SERVICE CHECK ===");
            response.put("analyticsService", analyticsService != null ? "INJECTED" : "NULL");
            response.put("totalRecords", analyticsService.getTotalRecords());
            response.put("status", "success");
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/scan/enhanced")
    public EnhancedScanResult enhancedScan(@RequestBody ScanRequest request) {
        return breachService.enhancedScan(request); // Now returns correct type
    }

}