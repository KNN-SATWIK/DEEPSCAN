package com.deepscan.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "analytics_records")
public class AnalyticsRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", length = 500)
    private String email;

    @Column(name = "password_text", columnDefinition = "TEXT")
    private String passwordText;

    @Column(name = "password_provided")
    private Boolean passwordProvided = false;

    @Column(name = "risk_score")
    private Integer riskScore = 0;

    @Column(name = "compromised")
    private Boolean compromised = false;

    @Column(name = "breach_count")
    private Integer breachCount = 0;

    @Column(name = "scan_type", length = 100)
    private String scanType = "analytics";

    @Column(name = "additional_data", columnDefinition = "TEXT")
    private String additionalData;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public AnalyticsRecord() {
        this.createdAt = LocalDateTime.now();
    }

    public AnalyticsRecord(String email, String password) {
        this();
        this.email = email;
        this.passwordText = password;
        this.passwordProvided = (password != null && !password.trim().isEmpty());
        this.scanType = "user_analytics";
        this.additionalData = "User submitted for analytics improvement";
    }

    // Getters and setters (keep all your existing ones)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordText() { return passwordText; }
    public void setPasswordText(String passwordText) { this.passwordText = passwordText; }

    public Boolean getPasswordProvided() { return passwordProvided; }
    public void setPasswordProvided(Boolean passwordProvided) { this.passwordProvided = passwordProvided; }

    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }

    public Boolean getCompromised() { return compromised; }
    public void setCompromised(Boolean compromised) { this.compromised = compromised; }

    public Integer getBreachCount() { return breachCount; }
    public void setBreachCount(Integer breachCount) { this.breachCount = breachCount; }

    public String getScanType() { return scanType; }
    public void setScanType(String scanType) { this.scanType = scanType; }

    public String getAdditionalData() { return additionalData; }
    public void setAdditionalData(String additionalData) { this.additionalData = additionalData; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}