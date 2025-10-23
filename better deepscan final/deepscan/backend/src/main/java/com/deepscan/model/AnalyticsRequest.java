package com.deepscan.model;

public class AnalyticsRequest {
    private String email;
    private String password;

    public AnalyticsRequest() {}

    public AnalyticsRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return "AnalyticsRequest{" +
                "email='" + email + '\'' +
                ", passwordProvided=" + (password != null && !password.isEmpty()) +
                '}';
    }
}