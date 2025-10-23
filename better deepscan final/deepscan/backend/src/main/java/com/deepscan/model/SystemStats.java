package com.deepscan.model;

import java.time.LocalDateTime;

public class SystemStats {
    private int monitoredEmails;
    private long totalBreaches;
    private int deepwebSources;
    private LocalDateTime lastUpdate;
    private boolean torAvailable;

    public SystemStats(int monitoredEmails, long totalBreaches, int deepwebSources, LocalDateTime lastUpdate, boolean torAvailable) {
        this.monitoredEmails = monitoredEmails;
        this.totalBreaches = totalBreaches;
        this.deepwebSources = deepwebSources;
        this.lastUpdate = lastUpdate;
        this.torAvailable = torAvailable;
    }

    // Getters and setters
    public int getMonitoredEmails() { return monitoredEmails; }
    public void setMonitoredEmails(int monitoredEmails) { this.monitoredEmails = monitoredEmails; }

    public long getTotalBreaches() { return totalBreaches; }
    public void setTotalBreaches(long totalBreaches) { this.totalBreaches = totalBreaches; }

    public int getDeepwebSources() { return deepwebSources; }
    public void setDeepwebSources(int deepwebSources) { this.deepwebSources = deepwebSources; }

    public LocalDateTime getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }

    public boolean isTorAvailable() { return torAvailable; }
    public void setTorAvailable(boolean torAvailable) { this.torAvailable = torAvailable; }
}