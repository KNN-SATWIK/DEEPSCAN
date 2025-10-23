package com.deepscan.model;

public class HibpResult {
    private boolean compromised;
    private int breachCount;

    public HibpResult(boolean compromised, int breachCount) {
        this.compromised = compromised;
        this.breachCount = breachCount;
    }

    // Getters and setters
    public boolean isCompromised() { return compromised; }
    public void setCompromised(boolean compromised) { this.compromised = compromised; }

    public int getBreachCount() { return breachCount; }
    public void setBreachCount(int breachCount) { this.breachCount = breachCount; }
}