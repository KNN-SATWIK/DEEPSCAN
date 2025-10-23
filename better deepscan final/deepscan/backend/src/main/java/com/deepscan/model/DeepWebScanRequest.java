package com.deepscan.model;

public class DeepWebScanRequest {
    private String email;
    private boolean deepScan;

    public DeepWebScanRequest() {}

    public DeepWebScanRequest(String email, boolean deepScan) {
        this.email = email;
        this.deepScan = deepScan;
    }

    // Getters and setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isDeepScan() { return deepScan; }
    public void setDeepScan(boolean deepScan) { this.deepScan = deepScan; }
}