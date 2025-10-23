package com.deepscan.model;

import java.time.LocalDateTime;
import java.util.List;

public class DeepWebResult {
    private String email;
    private List<DeepWebFinding> findings;
    private String status;
    private LocalDateTime scanTime;

    public DeepWebResult(String email, List<DeepWebFinding> findings, String status, LocalDateTime scanTime) {
        this.email = email;
        this.findings = findings;
        this.status = status;
        this.scanTime = scanTime;
    }

    // Getters and setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<DeepWebFinding> getFindings() { return findings; }
    public void setFindings(List<DeepWebFinding> findings) { this.findings = findings; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getScanTime() { return scanTime; }
    public void setScanTime(LocalDateTime scanTime) { this.scanTime = scanTime; }
}