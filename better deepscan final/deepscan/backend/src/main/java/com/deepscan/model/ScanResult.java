package com.deepscan.model;

import java.time.LocalDateTime;
import java.util.List;

public class ScanResult {
    private String email;
    private boolean compromised;
    private int breachCount;
    private List<DeepWebFinding> deepwebFindings;
    private int riskScore;
    private String threatAnalysis;
    private LocalDateTime scanTime;
    private boolean credentialsForSale;
    private boolean torEnabled;
    private String torStatus;

    // Constructor
    public ScanResult(String email, boolean compromised, int breachCount,
                      List<DeepWebFinding> deepwebFindings, int riskScore,
                      String threatAnalysis, LocalDateTime scanTime,
                      boolean credentialsForSale, boolean torEnabled, String torStatus) {
        this.email = email;
        this.compromised = compromised;
        this.breachCount = breachCount;
        this.deepwebFindings = deepwebFindings;
        this.riskScore = riskScore;
        this.threatAnalysis = threatAnalysis;
        this.scanTime = scanTime;
        this.credentialsForSale = credentialsForSale;
        this.torEnabled = torEnabled;
        this.torStatus = torStatus;
    }

    // Getters and setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isCompromised() { return compromised; }
    public void setCompromised(boolean compromised) { this.compromised = compromised; }

    public int getBreachCount() { return breachCount; }
    public void setBreachCount(int breachCount) { this.breachCount = breachCount; }

    public List<DeepWebFinding> getDeepwebFindings() { return deepwebFindings; }
    public void setDeepwebFindings(List<DeepWebFinding> deepwebFindings) { this.deepwebFindings = deepwebFindings; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public String getThreatAnalysis() { return threatAnalysis; }
    public void setThreatAnalysis(String threatAnalysis) { this.threatAnalysis = threatAnalysis; }

    public LocalDateTime getScanTime() { return scanTime; }
    public void setScanTime(LocalDateTime scanTime) { this.scanTime = scanTime; }

    public boolean isCredentialsForSale() { return credentialsForSale; }
    public void setCredentialsForSale(boolean credentialsForSale) { this.credentialsForSale = credentialsForSale; }

    public boolean isTorEnabled() { return torEnabled; }
    public void setTorEnabled(boolean torEnabled) { this.torEnabled = torEnabled; }

    public String getTorStatus() { return torStatus; }
    public void setTorStatus(String torStatus) { this.torStatus = torStatus; }
}