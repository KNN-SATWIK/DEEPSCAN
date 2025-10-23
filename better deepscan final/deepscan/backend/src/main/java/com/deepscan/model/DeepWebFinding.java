package com.deepscan.model;

import java.time.LocalDateTime;

public class DeepWebFinding {
    private String source;
    private String description;
    private String severity;
    private LocalDateTime foundDate;
    private boolean torSource;

    public DeepWebFinding(String source, String description, String severity, LocalDateTime foundDate, boolean torSource) {
        this.source = source;
        this.description = description;
        this.severity = severity;
        this.foundDate = foundDate;
        this.torSource = torSource;
    }

    // Getters and setters
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public LocalDateTime getFoundDate() { return foundDate; }
    public void setFoundDate(LocalDateTime foundDate) { this.foundDate = foundDate; }

    public boolean isTorSource() { return torSource; }
    public void setTorSource(boolean torSource) { this.torSource = torSource; }
}