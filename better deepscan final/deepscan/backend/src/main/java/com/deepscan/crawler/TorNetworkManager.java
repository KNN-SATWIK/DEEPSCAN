package com.deepscan.crawler;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TorNetworkManager {

    private static final String TOR_PROXY_HOST = "127.0.0.1";
    private static final int TOR_SOCKS_PORT = 9050;
    private static final int TOR_BROWSER_PORT = 9150;

    private CloseableHttpClient torHttpClient;
    private boolean torEnabled = false;

    public TorNetworkManager() {
        initializeTorClient();
    }

    private void initializeTorClient() {
        HttpHost proxy = tryCreateProxy(TOR_BROWSER_PORT);
        if (proxy == null) {
            proxy = tryCreateProxy(TOR_SOCKS_PORT);
        }

        if (proxy != null) {
            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);

            this.torHttpClient = HttpClients.custom()
                    .setRoutePlanner(routePlanner)
                    .build();

            this.torEnabled = true;
            System.out.println("✅ Tor network connected successfully on port " + proxy.getPort());
        } else {
            System.out.println("❌ Tor not available - install Tor Browser for real dark web scanning");
        }
    }

    private HttpHost tryCreateProxy(int port) {
        try {
            HttpHost proxy = new HttpHost("socks", TOR_PROXY_HOST, port);
            testTorConnection(proxy);
            return proxy;
        } catch (Exception e) {
            System.out.println("❌ Tor port " + port + " not available: " + e.getMessage());
            return null;
        }
    }

    private void testTorConnection(HttpHost proxy) throws IOException {
        CloseableHttpClient testClient = HttpClients.custom()
                .setRoutePlanner(new DefaultProxyRoutePlanner(proxy))
                .build();

        HttpGet testRequest = new HttpGet("http://check.torproject.org/");
        try (CloseableHttpResponse response = testClient.execute(testRequest)) {
            String content = EntityUtils.toString(response.getEntity());
            if (!content.contains("Congratulations")) {
                throw new IOException("Not connected to Tor");
            }
        } catch (Exception e) {
            throw new IOException("Tor test failed: " + e.getMessage());
        }
    }

    public String fetchViaTor(String onionUrl) throws IOException {
        if (!torEnabled) {
            throw new IOException("Tor network not available");
        }

        HttpGet request = new HttpGet(onionUrl);
        request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0");
        request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

        try (CloseableHttpResponse response = torHttpClient.execute(request)) {
            if (response.getCode() == 200) {
                return EntityUtils.toString(response.getEntity());
            } else {
                throw new IOException("Tor request failed with status: " + response.getCode());
            }
        } catch (Exception e) {
            throw new IOException("Tor request failed: " + e.getMessage());
        }
    }

    public boolean isTorConnected() {
        return torEnabled;
    }

    public String getTorStatus() {
        if (!torEnabled) {
            return "DISABLED - Tor Browser not installed or not running";
        }
        return "ACTIVE - Connected to Tor network";
    }
}