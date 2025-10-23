package com.deepscan.model;

public class ScanRequest {
    private String email;
    private String password;
    private boolean deepScan;
    private boolean enableTor; // Changed from isEnableTor() to enableTor
    private boolean scanEmailOnly;
    private boolean scanPasswordOnly;

    public ScanRequest() {}

    // Getters and setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isDeepScan() { return deepScan; }
    public void setDeepScan(boolean deepScan) { this.deepScan = deepScan; }

    public boolean isEnableTor() { return enableTor; } // Fixed method name
    public void setEnableTor(boolean enableTor) { this.enableTor = enableTor; }

    public boolean isScanEmailOnly() { return scanEmailOnly; }
    public void setScanEmailOnly(boolean scanEmailOnly) { this.scanEmailOnly = scanEmailOnly; }

    public boolean isScanPasswordOnly() { return scanPasswordOnly; }
    public void setScanPasswordOnly(boolean scanPasswordOnly) { this.scanPasswordOnly = scanPasswordOnly; }
}