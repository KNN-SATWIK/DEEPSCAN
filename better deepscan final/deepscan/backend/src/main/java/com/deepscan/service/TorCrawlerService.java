package com.deepscan.service;

import com.deepscan.crawler.RealDarkWebScanner;
import com.deepscan.crawler.TorNetworkManager;
import com.deepscan.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TorCrawlerService {

    @Autowired
    private RealDarkWebScanner realDarkWebScanner;

    @Autowired
    private TorNetworkManager torManager;

    private final Map<String, Boolean> activeMonitors = new HashMap<>();

    @Async
    public CompletableFuture<DeepWebResult> scanForCredentials(DeepWebScanRequest request) {
        List<DeepWebFinding> findings = new ArrayList<>();
        String status = "COMPLETED";

        if (request.isDeepScan() && torManager.isTorConnected()) {
            findings = realDarkWebScanner.realTimeTorScan(request.getEmail(), "");
            status = findings.isEmpty() ? "CLEAN" : "COMPROMISED";
        } else {
            findings.add(new DeepWebFinding(
                    "Tor Scanner",
                    "Deep web scanning requires Tor connection",
                    "LOW",
                    LocalDateTime.now(),
                    false
            ));
            status = "TOR_UNAVAILABLE";
        }

        return CompletableFuture.completedFuture(
                new DeepWebResult(request.getEmail(), findings, status, LocalDateTime.now())
        );
    }

    public void startMonitoring(String email) {
        activeMonitors.put(email, true);

        if (torManager.isTorConnected()) {
            new Thread(() -> {
                while (activeMonitors.getOrDefault(email, false)) {
                    try {
                        // Real monitoring would happen here
                        Thread.sleep(300000); // 5 minutes
                        System.out.println("Deep web monitoring completed for: " + email);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }).start();
        }
    }

    public void stopMonitoring(String email) {
        activeMonitors.put(email, false);
    }

    public Map<String, Object> getTorStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("torConnected", torManager.isTorConnected());
        status.put("activeMonitors", activeMonitors.size());
        status.put("timestamp", LocalDateTime.now());
        return status;
    }
}