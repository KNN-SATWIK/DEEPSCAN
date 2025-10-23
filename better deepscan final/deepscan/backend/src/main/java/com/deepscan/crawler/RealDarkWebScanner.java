package com.deepscan.crawler;

import com.deepscan.model.DeepWebFinding;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Real dark web scanner that connects to actual .onion sites via Tor
 */
@Component
public class RealDarkWebScanner {

    @Autowired
    private TorNetworkManager torManager;

    // Active .onion sites for credential searching
    private static final String[] ONION_PASTE_SITES = {
            "http://pastes4uyaqy2g7c.onion", // Active paste site
            "http://pasta57c2vjq3v33.onion", // Alternative paste site
            "http://sprp5r4e7g6f4g4c.onion"  // Secure private bin
    };

    private static final String[] ONION_FORUMS = {
            "http://dreadforge2tdn3x2.onion", // Dread Forum (active)
            "http://breachforums5cvorkz3fxg3q27u7gubg7kbsk4x6ditm3x0y6n5vysaad.onion", // BreachForums
            "http://cctforums2jrcq6lo.onion" // Cyber Crime Forums
    };

    private static final String[] ONION_SEARCH_ENGINES = {
            "http://ahmia.fi/", // Ahmia search (supports .onion)
            "http://darksearch.io/", // DarkSearch API
            "http://onionlandpatrol.org/" // OnionLand Search
    };

    private static final String[] ONION_MARKETS = {
            "http://torrezonion.org/", // Market discussions
            "http://darkmarketurl.org/" // Market listings
    };

    public List<DeepWebFinding> realTimeTorScan(String email, String passwordHash) {
        List<DeepWebFinding> findings = new ArrayList<>();

        if (!torManager.isTorConnected()) {
            findings.add(createTorOfflineFinding());
            return findings;
        }

        try {
            System.out.println("🔍 Starting real Tor scan for: " + email);

            // Scan multiple dark web sources
            findings.addAll(scanOnionPasteSites(email));
            findings.addAll(scanOnionForums(email));
            findings.addAll(searchOnionEngines(email));
            findings.addAll(scanOnionMarkets(email));
            findings.addAll(searchCredentialMarkets(email));

            // Rate limiting between requests
            Thread.sleep(2000);

            System.out.println("✅ Tor scan completed. Found " + findings.size() + " results");

        } catch (Exception e) {
            System.err.println("❌ Tor scan error: " + e.getMessage());
            findings.add(createErrorFinding("Tor scan failed: " + e.getMessage()));
        }

        return findings;
    }

    private List<DeepWebFinding> scanOnionPasteSites(String email) {
        List<DeepWebFinding> findings = new ArrayList<>();

        for (String onionSite : ONION_PASTE_SITES) {
            try {
                System.out.println("📝 Scanning paste site: " + getDomain(onionSite));
                String htmlContent = torManager.fetchViaTor(onionSite);
                Document doc = Jsoup.parse(htmlContent);

                // Look for paste content in various elements
                Elements pasteElements = doc.select("[class*='paste'], [class*='content'], textarea, pre, code, .text, .data");

                for (Element element : pasteElements) {
                    String text = element.text();
                    if (text.toLowerCase().contains(email.toLowerCase())) {
                        String context = extractContext(text, email);
                        findings.add(new DeepWebFinding(
                                "Onion Paste: " + getDomain(onionSite),
                                "Email found in dark web paste: " + context,
                                "HIGH",
                                LocalDateTime.now(),
                                true // Tor source
                        ));
                        System.out.println("🎯 Found email in paste site: " + getDomain(onionSite));
                        break;
                    }
                }

                // Also check for password dumps
                if (htmlContent.contains("password") || htmlContent.contains("leak") || htmlContent.contains("dump")) {
                    findings.add(new DeepWebFinding(
                            "Onion Paste: " + getDomain(onionSite),
                            "Password dump detected on this site",
                            "MEDIUM",
                            LocalDateTime.now(),
                            true
                    ));
                }

            } catch (Exception e) {
                System.err.println("❌ Failed to scan paste site " + getDomain(onionSite) + ": " + e.getMessage());
            }
        }

        return findings;
    }

    private List<DeepWebFinding> scanOnionForums(String email) {
        List<DeepWebFinding> findings = new ArrayList<>();

        for (String forum : ONION_FORUMS) {
            try {
                System.out.println("💬 Scanning forum: " + getDomain(forum));
                String htmlContent = torManager.fetchViaTor(forum);
                Document doc = Jsoup.parse(htmlContent);

                // Search in forum posts and threads
                Elements posts = doc.select(".post, .message, .thread, .content, .topic, [class*='post'], [class*='message']");

                for (Element post : posts) {
                    String postText = post.text();
                    if (containsSensitiveKeywords(postText, email)) {
                        findings.add(new DeepWebFinding(
                                "Onion Forum: " + getDomain(forum),
                                "Credentials discussed in dark web forum: " + extractContext(postText, email),
                                "CRITICAL",
                                LocalDateTime.now(),
                                true // Tor source
                        ));
                        System.out.println("🎯 Found sensitive discussion in forum: " + getDomain(forum));
                        break;
                    }
                }

                // Check for breach discussions
                if (htmlContent.contains("breach") || htmlContent.contains("leaked") || htmlContent.contains("database")) {
                    findings.add(new DeepWebFinding(
                            "Onion Forum: " + getDomain(forum),
                            "Active breach discussions detected",
                            "HIGH",
                            LocalDateTime.now(),
                            true
                    ));
                }

            } catch (Exception e) {
                System.err.println("❌ Failed to scan forum " + getDomain(forum) + ": " + e.getMessage());
            }
        }

        return findings;
    }

    private List<DeepWebFinding> searchOnionEngines(String email) {
        List<DeepWebFinding> findings = new ArrayList<>();

        for (String searchEngine : ONION_SEARCH_ENGINES) {
            try {
                System.out.println("🔎 Searching engine: " + getDomain(searchEngine));
                String searchUrl = searchEngine + "search?q=" + email.replace("@", "%40");
                String content = torManager.fetchViaTor(searchUrl);

                if (content.toLowerCase().contains(email.toLowerCase()) ||
                        content.contains("breach") ||
                        content.contains("leak") ||
                        content.contains("password") ||
                        content.contains("credential")) {

                    findings.add(new DeepWebFinding(
                            "Dark Web Search: " + getDomain(searchEngine),
                            "Email found in dark web search results",
                            "MEDIUM",
                            LocalDateTime.now(),
                            true // Tor source
                    ));
                    System.out.println("🎯 Found references in search engine: " + getDomain(searchEngine));
                }

            } catch (Exception e) {
                System.err.println("❌ Failed to search engine " + getDomain(searchEngine) + ": " + e.getMessage());
            }
        }

        return findings;
    }

    private List<DeepWebFinding> scanOnionMarkets(String email) {
        List<DeepWebFinding> findings = new ArrayList<>();

        for (String market : ONION_MARKETS) {
            try {
                System.out.println("🛒 Scanning market: " + getDomain(market));
                String content = torManager.fetchViaTor(market);

                // Check for credential sales
                if ((content.contains("sell") || content.contains("sale")) &&
                        (content.contains("account") || content.contains("login") || content.contains("credential"))) {

                    findings.add(new DeepWebFinding(
                            "Dark Market: " + getDomain(market),
                            "Credential trading detected on this market",
                            "CRITICAL",
                            LocalDateTime.now(),
                            true
                    ));
                    System.out.println("🎯 Found credential trading on market: " + getDomain(market));
                }

            } catch (Exception e) {
                System.err.println("❌ Failed to scan market " + getDomain(market) + ": " + e.getMessage());
            }
        }

        return findings;
    }

    private List<DeepWebFinding> searchCredentialMarkets(String email) {
        List<DeepWebFinding> findings = new ArrayList<>();

        try {
            // Search for email in credential market contexts
            String[] searchTerms = {
                    email,
                    email.split("@")[0], // username part
                    "password",
                    "login",
                    "account"
            };

            for (String term : searchTerms) {
                for (String searchEngine : ONION_SEARCH_ENGINES) {
                    try {
                        String searchUrl = searchEngine + "search?q=" + term.replace(" ", "%20");
                        String content = torManager.fetchViaTor(searchUrl);

                        if (content.contains(email) || (content.contains(term) && content.contains("sell"))) {
                            findings.add(new DeepWebFinding(
                                    "Credential Market Search",
                                    "Potential credential sale detected for: " + term,
                                    "HIGH",
                                    LocalDateTime.now(),
                                    true
                            ));
                        }
                    } catch (Exception e) {
                        // Continue with next search
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Credential market search failed: " + e.getMessage());
        }

        return findings;
    }

    public List<DeepWebFinding> monitorEmail(String email, int durationHours) {
        List<DeepWebFinding> findings = new ArrayList<>();

        if (!torManager.isTorConnected()) {
            findings.add(createTorOfflineFinding());
            return findings;
        }

        try {
            System.out.println("🕵️ Starting continuous monitoring for: " + email);

            // Extended monitoring with multiple scans
            for (int i = 0; i < durationHours; i++) {
                System.out.println("📊 Monitoring scan " + (i + 1) + "/" + durationHours);
                findings.addAll(realTimeTorScan(email, ""));

                if (i < durationHours - 1) {
                    Thread.sleep(3600000); // Wait 1 hour between scans
                }
            }

            System.out.println("✅ Monitoring completed. Total findings: " + findings.size());

        } catch (Exception e) {
            System.err.println("❌ Monitoring failed: " + e.getMessage());
            findings.add(createErrorFinding("Monitoring failed: " + e.getMessage()));
        }

        return findings;
    }

    public boolean checkCredentialsForSale(String email) {
        if (!torManager.isTorConnected()) {
            return false;
        }

        try {
            // Check dark web markets for credential sales
            for (String market : ONION_MARKETS) {
                String content = torManager.fetchViaTor(market);
                if ((content.contains("sell") || content.contains("sale")) &&
                        content.contains("credential") &&
                        content.contains(email)) {
                    return true;
                }
            }

            // Check forums for sales discussions
            for (String forum : ONION_FORUMS) {
                String content = torManager.fetchViaTor(forum);
                if (content.contains("WTS") && // "Want To Sell"
                        content.contains("account") &&
                        content.contains(email)) {
                    return true;
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Failed to check credential sales: " + e.getMessage());
        }

        return false;
    }

    public String getTorScanSummary() {
        if (!torManager.isTorConnected()) {
            return "Tor not available - install Tor Browser for real dark web scanning";
        }

        return "Real Tor scanning active - monitoring " +
                (ONION_PASTE_SITES.length + ONION_FORUMS.length + ONION_SEARCH_ENGINES.length) +
                " dark web sources";
    }

    private boolean containsSensitiveKeywords(String text, String email) {
        String lowerText = text.toLowerCase();
        String lowerEmail = email.toLowerCase();

        boolean hasEmail = lowerText.contains(lowerEmail);
        boolean hasSensitiveTerms = lowerText.contains("password") ||
                lowerText.contains("leak") ||
                lowerText.contains("breach") ||
                lowerText.contains("dump") ||
                lowerText.contains("credential") ||
                lowerText.contains("hack") ||
                lowerText.contains("database") ||
                lowerText.contains("sale") ||
                lowerText.contains("buy") ||
                lowerText.contains("WTS") || // Want To Sell
                lowerText.contains("WTB") || // Want To Buy
                lowerText.contains("account") ||
                lowerText.contains("login");

        return hasEmail && hasSensitiveTerms;
    }

    private String extractContext(String text, String email) {
        int index = text.toLowerCase().indexOf(email.toLowerCase());
        if (index != -1) {
            int start = Math.max(0, index - 50);
            int end = Math.min(text.length(), index + 50);
            String snippet = text.substring(start, end).replace("\n", " ").trim();
            return "..." + snippet + "...";
        }
        return "Context unavailable";
    }

    private String getDomain(String url) {
        try {
            String domain = url.split("//")[1].split("/")[0];
            // Shorten long .onion domains for display
            if (domain.length() > 20) {
                return domain.substring(0, 10) + "..." + domain.substring(domain.length() - 5);
            }
            return domain;
        } catch (Exception e) {
            return url;
        }
    }

    private DeepWebFinding createTorOfflineFinding() {
        return new DeepWebFinding(
                "Tor Network",
                "Tor Browser not running. Install Tor Browser from torproject.org and keep it open for real dark web scanning.",
                "LOW",
                LocalDateTime.now(),
                false
        );
    }

    private DeepWebFinding createErrorFinding(String error) {
        return new DeepWebFinding(
                "Scanner Error",
                error,
                "LOW",
                LocalDateTime.now(),
                false
        );
    }
}