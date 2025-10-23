package com.deepscan.service;

import com.deepscan.crawler.DarkWebScanner;
import com.deepscan.crawler.TorNetworkManager;
import com.deepscan.model.*;
import com.deepscan.repository.BreachRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BreachService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DarkWebScanner darkWebScanner;

    @Autowired
    private TorNetworkManager torManager;

    @Autowired
    private BreachRepository breachRepository;

    @Autowired
    private EmailValidationService emailValidationService;

    @Autowired
    private AnalyticsService analyticsService;

    private static final String HIBP_API = "https://api.pwnedpasswords.com/range/";

    @Autowired
    public void initializeSampleData() {
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        if (breachRepository.count() == 0) {
                            System.out.println("📊 Initializing sample breach data...");
                            List<BreachRecord> sampleRecords = Arrays.asList(
                                    new BreachRecord("user@example.com", hashSHA1("password123"), "Example Breach 2023", "2023-01-15"),
                                    new BreachRecord("test@gmail.com", hashSHA1("123456"), "Test Breach 2023", "2023-02-20"),
                                    new BreachRecord("admin@example.com", hashSHA1("admin"), "Admin Breach 2023", "2023-03-10")
                            );
                            breachRepository.saveAll(sampleRecords);
                            System.out.println("✅ Sample data loaded: " + sampleRecords.size() + " records");
                        } else {
                            System.out.println("📊 Database already contains " + breachRepository.count() + " breach records");
                        }
                    }
                },
                2000
        );
    }

    public ScanResult fullScan(ScanRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();
        boolean enableTor = request.isDeepScan(); // Use deepScan instead of enableTor

        HibpResult hibpResult = checkHibpPassword(password);
        List<DeepWebFinding> deepwebFindings = darkWebScanner.scanCredentials(email, hashSHA1(password), enableTor);
        int riskScore = calculateRiskScore(hibpResult, deepwebFindings);
        String torStatus = getTorStatus(enableTor);

        return new ScanResult(
                email,
                hibpResult.isCompromised(),
                hibpResult.getBreachCount(),
                deepwebFindings,
                riskScore,
                generateThreatAnalysis(hibpResult, deepwebFindings),
                LocalDateTime.now(),
                false,
                enableTor,
                torStatus
        );
    }

    public EnhancedScanResult enhancedScan(ScanRequest request) {
        EnhancedScanResult result = new EnhancedScanResult();
        result.setEmail(request.getEmail());
        result.setScanTime(LocalDateTime.now());

        List<String> findings = new ArrayList<>();
        List<DeepWebFinding> deepwebFindings = new ArrayList<>();
        int riskScore = 0;
        boolean compromised = false;
        int breachCount = 0;

        try {
            // Email scanning
            // Email scanning
            if (!request.isScanPasswordOnly() && request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                EmailValidationService.EmailValidationResult emailResult = emailValidationService.validateEmail(request.getEmail());

                if (!emailResult.isValid()) {
                    findings.add("❌ Invalid email format");
                    riskScore += 40;
                } else {
                    findings.add("✅ Email format: Valid");

                    // Only add risk if issues are actually detected
                    if (emailResult.getDisposable() != null && emailResult.getDisposable()) {
                        findings.add("🚨 Disposable email detected - high risk");
                        riskScore += 30;
                    } else {
                        findings.add("✅ Not a disposable email");
                    }

                    if (emailResult.getFreeEmail() != null && emailResult.getFreeEmail()) {
                        findings.add("🔍 Free email provider - moderate risk");
                        riskScore += 10;
                    } else {
                        findings.add("✅ Professional email domain");
                    }

                    // Deliverability check - only add risk if we know it's not deliverable
                    if (emailResult.getDeliverable() != null && !emailResult.getDeliverable()) {
                        findings.add("⚠️ Email may not be deliverable");
                        riskScore += 20;
                    } else {
                        findings.add("✅ Email appears deliverable");
                    }
                }
            }

            // Password scanning
            if (!request.isScanEmailOnly() && request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
                HibpResult hibpResult = checkHibpPassword(request.getPassword());
                compromised = hibpResult.isCompromised();
                breachCount = hibpResult.getBreachCount();

                if (compromised) {
                    findings.add("🚨 Password found in " + breachCount + " data breaches");
                    riskScore += Math.min(breachCount * 5, 50);
                } else {
                    findings.add("✅ Password not found in known breaches");
                }

                String strength = assessPasswordStrength(request.getPassword());
                findings.add("🔒 Password strength: " + strength);

                switch (strength) {
                    case "Very Weak": riskScore += 30; break;
                    case "Weak": riskScore += 20; break;
                    case "Moderate": riskScore += 10; break;
                    case "Strong": riskScore -= 5; break;
                    case "Very Strong": riskScore -= 10; break;
                }

                if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                    deepwebFindings = darkWebScanner.scanCredentials(
                            request.getEmail(), hashSHA1(request.getPassword()), request.isDeepScan()
                    );

                    for (DeepWebFinding finding : deepwebFindings) {
                        findings.add("🌐 " + finding.getSource() + ": " + finding.getDescription());
                        switch (finding.getSeverity()) {
                            case "CRITICAL": riskScore += 25; break;
                            case "HIGH": riskScore += 15; break;
                            case "MEDIUM": riskScore += 10; break;
                            case "LOW": riskScore += 5; break;
                        }
                    }
                }
            }

            riskScore = Math.max(0, Math.min(100, riskScore));

            result.setRiskScore(riskScore);
            result.setCompromised(compromised);
            result.setBreachCount(breachCount);
            result.setFindings(findings);
            result.setDeepwebFindings(deepwebFindings);

            if (riskScore >= 80) {
                result.setThreatAnalysis("🚨 CRITICAL: Multiple high-risk factors detected");
            } else if (riskScore >= 60) {
                result.setThreatAnalysis("🔥 HIGH: Significant security concerns identified");
            } else if (riskScore >= 40) {
                result.setThreatAnalysis("⚠️ MEDIUM: Some security concerns present");
            } else if (riskScore >= 20) {
                result.setThreatAnalysis("🔍 LOW: Minor security notes");
            } else {
                result.setThreatAnalysis("✅ SECURE: No significant issues detected");
            }


        } catch (Exception e) {
            result.setRiskScore(0);
            result.setThreatAnalysis("❌ Scan failed: " + e.getMessage());
            findings.add("Scan error: " + e.getMessage());
        }

        return result;
    }

    private HibpResult checkHibpPassword(String password) {
        try {
            String fullHash = hashSHA1(password);
            String prefix = fullHash.substring(0, 5);
            String suffix = fullHash.substring(5);
            String response = restTemplate.getForObject(HIBP_API + prefix, String.class);

            if (response != null && response.toUpperCase().contains(suffix)) {
                int breachCount = extractBreachCount(response, suffix);
                return new HibpResult(true, breachCount);
            }
            return new HibpResult(false, 0);
        } catch (Exception e) {
            return new HibpResult(false, 0);
        }
    }

    private String assessPasswordStrength(String password) {
        if (password == null || password.length() < 6) return "Very Weak";
        if (password.length() < 8) return "Weak";

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        int strengthPoints = 0;
        if (hasUpper) strengthPoints++;
        if (hasLower) strengthPoints++;
        if (hasDigit) strengthPoints++;
        if (hasSpecial) strengthPoints++;
        if (password.length() >= 12) strengthPoints++;

        if (strengthPoints >= 5) return "Very Strong";
        if (strengthPoints >= 4) return "Strong";
        if (strengthPoints >= 3) return "Moderate";
        return "Weak";
    }

    private int calculateRiskScore(HibpResult hibp, List<DeepWebFinding> findings) {
        int score = hibp.isCompromised() ? 40 : 0;
        score += Math.min(hibp.getBreachCount() / 100000, 30);

        for (DeepWebFinding finding : findings) {
            switch (finding.getSeverity()) {
                case "CRITICAL": score += 25; break;
                case "HIGH": score += 15; break;
                case "MEDIUM": score += 10; break;
                case "LOW": score += 5; break;
            }
            if (finding.isTorSource()) score += 10;
        }
        return Math.min(score, 100);
    }

    private String generateThreatAnalysis(HibpResult hibp, List<DeepWebFinding> findings) {
        boolean hasRealTorFindings = findings.stream().anyMatch(DeepWebFinding::isTorSource);
        if (hasRealTorFindings) {
            return "🚨 CRITICAL: Credentials found on real dark web sites via Tor";
        } else if (hibp.isCompromised() && !findings.isEmpty()) {
            return "🔥 HIGH: Password breached and detected in underground sources";
        } else if (hibp.isCompromised()) {
            return "⚠️ MEDIUM: Password compromised in " + hibp.getBreachCount() + " breaches";
        } else if (!findings.isEmpty()) {
            return "🔍 SUSPICIOUS: Activity detected in dark web monitoring";
        } else {
            return "✅ CLEAN: No threats detected in our intelligence sources";
        }
    }

    private String getTorStatus(boolean enableTor) {
        if (!enableTor) {
            return "DISABLED - Tor scanning not enabled";
        } else if (torManager.isTorConnected()) {
            return "ACTIVE - Connected to Tor network";
        } else {
            return "UNAVAILABLE - Install Tor Browser for real dark web scanning";
        }
    }

    private String hashSHA1(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString().toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    private int extractBreachCount(String response, String suffix) {
        try {
            String[] lines = response.split("\n");
            for (String line : lines) {
                if (line.startsWith(suffix)) {
                    String[] parts = line.split(":");
                    return Integer.parseInt(parts[1].trim());
                }
            }
        } catch (Exception e) {}
        return 1;
    }

    public SystemStats getSystemStats() {
        return new SystemStats(
                0,
                1187654321L,
                84721,
                LocalDateTime.now(),
                torManager.isTorConnected()
        );
    }
}