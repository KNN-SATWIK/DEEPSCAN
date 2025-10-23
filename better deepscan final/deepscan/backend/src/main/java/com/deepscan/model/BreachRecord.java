package com.deepscan.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "breach_records")
public class BreachRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", length = 255, nullable = false)
    private String email;

    @Column(name = "password_hash", length = 64, nullable = false)
    private String passwordHash;

    @Column(name = "breach_source", length = 100)
    private String breachSource;

    @Column(name = "breach_date", length = 20)
    private String breachDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Default constructor
    public BreachRecord() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor for creating sample records
    public BreachRecord(String email, String passwordHash, String breachSource, String breachDate) {
        this();
        this.email = email;
        this.passwordHash = passwordHash;
        this.breachSource = breachSource;
        this.breachDate = breachDate;
    }

    @PreUpdate
    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getBreachSource() { return breachSource; }
    public void setBreachSource(String breachSource) { this.breachSource = breachSource; }

    public String getBreachDate() { return breachDate; }
    public void setBreachDate(String breachDate) { this.breachDate = breachDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "BreachRecord{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", breachSource='" + breachSource + '\'' +
                ", breachDate='" + breachDate + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}