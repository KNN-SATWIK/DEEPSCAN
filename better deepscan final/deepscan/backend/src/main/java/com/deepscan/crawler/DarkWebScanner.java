package com.deepscan.crawler;

import com.deepscan.model.DeepWebFinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Simulation-based dark web scanner (fallback when Tor is not available)
 */
@Component
public class DarkWebScanner {

    @Autowired
    private RealDarkWebScanner realScanner;

    @Autowired
    private TorNetworkManager torManager;

    private static final String[] SIMULATED_SOURCES = {
            "BreachForums", "RaidForums", "Nulled", "Cracked", "Dread",
            "Pastebin", "Ghostbin", "Telegram Channels", "DarkNet Markets"
    };

    private static final String[] SEVERITIES = {"LOW", "MEDIUM", "HIGH", "CRITICAL"};

    public List<DeepWebFinding> scanCredentials(String email, String passwordHash, boolean useRealTor) {
        if (useRealTor && torManager.isTorConnected()) {
            System.out.println("🕵️ Using real Tor scanner for: " + email);
            return realScanner.realTimeTorScan(email, passwordHash);
        } else {
            System.out.println("🎭 Using simulated scanner for: " + email);
            return simulateDarkWebFindings(email);
        }
    }

    private List<DeepWebFinding> simulateDarkWebFindings(String email) {
        List<DeepWebFinding> findings = new ArrayList<>();
        Random random = new Random(email.hashCode());

        // Simulate random findings based on email hash for consistency
        int findingCount = random.nextInt(3); // 0-2 findings

        for (int i = 0; i < findingCount; i++) {
            String source = SIMULATED_SOURCES[random.nextInt(SIMULATED_SOURCES.length)];
            String severity = SEVERITIES[random.nextInt(SEVERITIES.length)];
            String description = generateFindingDescription(email, source, severity, random);

            findings.add(new DeepWebFinding(
                    "Simulated: " + source,
                    description,
                    severity,
                    LocalDateTime.now().minusDays(random.nextInt(30)),
                    false // Not from real Tor
            ));
        }

        // Always add a note about Tor when in simulation mode
        if (findings.isEmpty()) {
            findings.add(new DeepWebFinding(
                    "Simulation Mode",
                    "No threats detected in simulated scan. Enable Tor for real dark web scanning.",
                    "LOW",
                    LocalDateTime.now(),
                    false
            ));
        }

        return findings;
    }

    private String generateFindingDescription(String email, String source, String severity, Random random) {
        String[] templates = {
                "Email found in %s database discussion",
                "Credentials mentioned in %s breach thread",
                "User data detected in %s leak archive",
                "Account information shared on %s platform",
                "Login details circulated in %s community"
        };

        String baseDescription = String.format(templates[random.nextInt(templates.length)], source.toLowerCase());

        // Add severity-specific details
        switch (severity) {
            case "CRITICAL":
                return baseDescription + " - Active trading detected";
            case "HIGH":
                return baseDescription + " - Recent exposure identified";
            case "MEDIUM":
                return baseDescription + " - Historical reference found";
            case "LOW":
                return baseDescription + " - Minor reference in archives";
            default:
                return baseDescription;
        }
    }

    public List<DeepWebFinding> simulateContinuousMonitoring(String email, int hours) {
        List<DeepWebFinding> findings = new ArrayList<>();
        Random random = new Random(email.hashCode() + hours);

        // Simulate monitoring findings over time
        for (int i = 0; i < hours; i++) {
            if (random.nextDouble() < 0.3) { // 30% chance per hour
                String source = SIMULATED_SOURCES[random.nextInt(SIMULATED_SOURCES.length)];
                findings.add(new DeepWebFinding(
                        "Monitoring: " + source,
                        "Real-time alert: New mention detected during monitoring",
                        random.nextDouble() < 0.7 ? "MEDIUM" : "HIGH",
                        LocalDateTime.now().minusHours(i),
                        false
                ));
            }
        }

        return findings;
    }

    public boolean simulateCredentialsForSale(String email) {
        Random random = new Random(email.hashCode());
        // More likely for common email domains
        return email.contains("@gmail.com") || email.contains("@yahoo.com") ?
                random.nextDouble() < 0.3 : random.nextDouble() < 0.1;
    }

    public double simulateCredentialPrice(String email) {
        Random random = new Random(email.hashCode());
        double basePrice = 1.0;

        // Adjust price based on email domain
        if (email.contains("@gmail.com") || email.contains("@yahoo.com")) {
            basePrice += random.nextDouble() * 4.0; // $1-5
        } else if (email.contains("@corporate.") || email.contains("@company.")) {
            basePrice += random.nextDouble() * 19.0; // $1-20
        } else {
            basePrice += random.nextDouble() * 2.0; // $1-3
        }

        return Math.round(basePrice * 100.0) / 100.0;
    }

    /**
     * Get scanner status information
     */
    public String getScannerStatus() {
        if (torManager.isTorConnected()) {
            return "ACTIVE - Real Tor scanning available";
        } else {
            return "SIMULATION - Tor not available, using simulated data";
        }
    }

    /**
     * Get recommendations based on scan results
     */
    public String getSecurityRecommendations(List<DeepWebFinding> findings) {
        if (findings.isEmpty()) {
            return "No immediate action required. Consider enabling Tor for deeper scanning.";
        }

        long criticalCount = findings.stream().filter(f -> f.getSeverity().equals("CRITICAL")).count();
        long highCount = findings.stream().filter(f -> f.getSeverity().equals("HIGH")).count();

        if (criticalCount > 0) {
            return "IMMEDIATE ACTION: Change password and enable two-factor authentication immediately.";
        } else if (highCount > 0) {
            return "URGENT: Change password and review account security settings.";
        } else {
            return "RECOMMENDED: Consider changing password as precaution.";
        }
    }
}