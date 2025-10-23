package com.deepscan.model;

public class MonitorRequest {
    private String email;
    private int duration; // hours

    public MonitorRequest() {}

    public MonitorRequest(String email, int duration) {
        this.email = email;
        this.duration = duration;
    }

    // Getters and setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
}