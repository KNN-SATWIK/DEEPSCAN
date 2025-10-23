class DeepScanApp {
    constructor() {
        this.apiBase = 'http://localhost:8080/api/v1';
        this.init();
    }

    init() {
        this.bindEvents();
        this.updateSystemStats();
        this.setupScanTypeButtons();
    }

    bindEvents() {
        document.getElementById('scanBtn').addEventListener('click', () => this.initiateScan());
        document.getElementById('analyticsBtn').addEventListener('click', () => this.addToAnalytics());

        // Enter key support
        document.getElementById('password').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.initiateScan();
        });

        // Scan type change handlers
        const scanEmailOnly = document.getElementById('scanEmailOnly');
        const scanPasswordOnly = document.getElementById('scanPasswordOnly');

        if (scanEmailOnly) {
            scanEmailOnly.addEventListener('change', () => this.updateScanType());
        }
        if (scanPasswordOnly) {
            scanPasswordOnly.addEventListener('change', () => this.updateScanType());
        }
    }

    setupScanTypeButtons() {
        // Create scan type buttons container
        const scanOptions = document.querySelector('.scan-options');
        if (!scanOptions) return;

        // Check if buttons already exist
        if (!document.getElementById('emailOnlyBtn')) {
            const buttonContainer = document.createElement('div');
            buttonContainer.className = 'scan-type-buttons';
            buttonContainer.innerHTML = `
                <button id="emailOnlyBtn" class="scan-type-btn" type="button">📧 Email Only</button>
                <button id="passwordOnlyBtn" class="scan-type-btn" type="button">🔒 Password Only</button>
                <button id="fullScanBtn" class="scan-type-btn active" type="button">🛡️ Full Scan</button>
            `;

            scanOptions.parentNode.insertBefore(buttonContainer, scanOptions);

            // Add event listeners
            document.getElementById('emailOnlyBtn').addEventListener('click', () => this.setScanType('email'));
            document.getElementById('passwordOnlyBtn').addEventListener('click', () => this.setScanType('password'));
            document.getElementById('fullScanBtn').addEventListener('click', () => this.setScanType('full'));
        }
    }

    setScanType(type) {
        const emailOnly = document.getElementById('scanEmailOnly');
        const passwordOnly = document.getElementById('scanPasswordOnly');

        if (emailOnly && passwordOnly) {
            switch(type) {
                case 'email':
                    emailOnly.checked = true;
                    passwordOnly.checked = false;
                    break;
                case 'password':
                    emailOnly.checked = false;
                    passwordOnly.checked = true;
                    break;
                case 'full':
                    emailOnly.checked = false;
                    passwordOnly.checked = false;
                    break;
            }
        }

        // Update button states
        document.querySelectorAll('.scan-type-btn').forEach(btn => btn.classList.remove('active'));
        const targetBtn = document.getElementById(type + 'OnlyBtn');
        if (targetBtn) {
            targetBtn.classList.add('active');
        }

        // Update input behavior
        this.updateInputBehavior(type);
    }

    updateScanType() {
        const emailOnly = document.getElementById('scanEmailOnly').checked;
        const passwordOnly = document.getElementById('scanPasswordOnly').checked;

        if (emailOnly) {
            this.setScanType('email');
        } else if (passwordOnly) {
            this.setScanType('password');
        } else {
            this.setScanType('full');
        }
    }

    updateInputBehavior(type) {
        const emailInput = document.getElementById('email');
        const passwordInput = document.getElementById('password');

        if (emailInput && passwordInput) {
            switch(type) {
                case 'email':
                    emailInput.placeholder = 'Enter email to scan...';
                    passwordInput.placeholder = 'Password not required';
                    passwordInput.disabled = true;
                    emailInput.disabled = false;
                    break;
                case 'password':
                    emailInput.placeholder = 'Email not required';
                    passwordInput.placeholder = 'Enter password to scan...';
                    emailInput.disabled = true;
                    passwordInput.disabled = false;
                    break;
                case 'full':
                    emailInput.placeholder = 'Enter email';
                    passwordInput.placeholder = 'Enter password';
                    emailInput.disabled = false;
                    passwordInput.disabled = false;
                    break;
            }
        }
    }

    async initiateScan() {
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;
        const deepScan = document.getElementById('deepScan').checked;
        const scanEmailOnly = document.getElementById('scanEmailOnly')?.checked || false;
        const scanPasswordOnly = document.getElementById('scanPasswordOnly')?.checked || false;
        const continuousMonitor = document.getElementById('continuousMonitor').checked;

        // Validate input based on scan type
        if (scanEmailOnly && !email) {
            this.addLogEntry('[ERROR] Email required for email-only scan', 'error');
            this.showError('Please enter an email address for email-only scan');
            return;
        }

        if (scanPasswordOnly && !password) {
            this.addLogEntry('[ERROR] Password required for password-only scan', 'error');
            this.showError('Please enter a password for password-only scan');
            return;
        }

        if (!scanEmailOnly && !scanPasswordOnly && (!email || !password)) {
            this.addLogEntry('[ERROR] Both email and password required for full scan', 'error');
            this.showError('Please enter both email and password for full scan');
            return;
        }

        this.showLoading();
        this.hideResults();

        const scanType = scanEmailOnly ? 'EMAIL' : scanPasswordOnly ? 'PASSWORD' : 'FULL';
        this.addLogEntry(`[SCAN] Initiating ${scanType} scan for: ${email || 'N/A'}`);

        try {
            // Use enhanced scan endpoint
            const response = await fetch(`${this.apiBase}/scan/enhanced`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    email: email,
                    password: password,
                    deepScan: deepScan,
                    scanEmailOnly: scanEmailOnly,
                    scanPasswordOnly: scanPasswordOnly
                })
            });

            if (!response.ok) throw new Error(`HTTP ${response.status}: ${response.statusText}`);

            const result = await response.json();
            console.log('Enhanced scan result:', result);

            // Use enhanced results display
            this.displayEnhancedResults(result);

            if (continuousMonitor && email) {
                this.startMonitoring(email);
            }
        } catch (error) {
            console.error('Scan error:', error);
            this.addLogEntry('[ERROR] Scan failed: ' + error.message, 'error');
            this.displayError('Scan failed - ' + error.message);
        } finally {
            this.hideLoading();
        }
    }

    // Enhanced results display method
    displayEnhancedResults(result) {
        const resultsPanel = document.getElementById('results');
        const riskFill = document.getElementById('riskFill');
        const riskValue = document.getElementById('riskValue');
        const riskLabel = document.getElementById('riskLabel');
        const threatText = document.getElementById('threatText');
        const findingsList = document.getElementById('findingsList');
        const scanTime = document.getElementById('scanTime');

        // Update risk meter
        riskFill.style.width = result.riskScore + '%';
        riskValue.textContent = result.riskScore + '%';

        // Set risk color and label
        if (result.riskScore >= 80) {
            riskFill.style.background = 'linear-gradient(90deg, #ff0000, #ff4444)';
            riskLabel.textContent = 'CRITICAL RISK';
            riskLabel.style.color = '#ff0000';
        } else if (result.riskScore >= 60) {
            riskFill.style.background = 'linear-gradient(90deg, #ff8000, #ffaa44)';
            riskLabel.textContent = 'HIGH RISK';
            riskLabel.style.color = '#ff8000';
        } else if (result.riskScore >= 40) {
            riskFill.style.background = 'linear-gradient(90deg, #ffff00, #ffff66)';
            riskLabel.textContent = 'MEDIUM RISK';
            riskLabel.style.color = '#ffff00';
        } else if (result.riskScore >= 20) {
            riskFill.style.background = 'linear-gradient(90deg, #00ff00, #44ff44)';
            riskLabel.textContent = 'LOW RISK';
            riskLabel.style.color = '#00ff00';
        } else {
            riskFill.style.background = 'linear-gradient(90deg, #00ff41, #0ff0fc)';
            riskLabel.textContent = 'SECURE';
            riskLabel.style.color = '#00ff41';
        }

        // Update threat analysis
        threatText.textContent = result.threatAnalysis || 'No threat analysis available';
        threatText.style.color = ''; // Reset color

        // Update findings - handle both old and new result formats
        findingsList.innerHTML = '';

        if (result.findings && result.findings.length > 0) {
            // New format with enhanced findings
            result.findings.forEach(finding => {
                const findingElement = document.createElement('div');
                findingElement.className = 'finding-item';

                // Color code findings based on content
                if (finding.includes('🚨') || finding.includes('CRITICAL') || finding.toLowerCase().includes('high risk')) {
                    findingElement.style.color = '#ff4444';
                    findingElement.style.borderLeft = '3px solid #ff4444';
                } else if (finding.includes('⚠️') || finding.includes('MEDIUM') || finding.toLowerCase().includes('moderate')) {
                    findingElement.style.color = '#ffff00';
                    findingElement.style.borderLeft = '3px solid #ffff00';
                } else if (finding.includes('✅') || finding.includes('SECURE') || finding.toLowerCase().includes('valid')) {
                    findingElement.style.color = '#00ff41';
                    findingElement.style.borderLeft = '3px solid #00ff41';
                } else {
                    findingElement.style.color = '#0ff0fc';
                    findingElement.style.borderLeft = '3px solid #0ff0fc';
                }

                findingElement.innerHTML = `
                    <div class="finding-desc">${finding}</div>
                `;
                findingsList.appendChild(findingElement);
            });
        } else if (result.deepwebFindings && result.deepwebFindings.length > 0) {
            // Old format with deepweb findings
            result.deepwebFindings.forEach(finding => {
                const findingElement = document.createElement('div');
                findingElement.className = 'finding-item';
                findingElement.innerHTML = `
                    <div class="finding-source">${finding.source}</div>
                    <div class="finding-desc">${finding.description}</div>
                    <div class="finding-meta">
                        <span class="finding-severity ${finding.severity.toLowerCase()}">${finding.severity}</span>
                        <span class="finding-date">${new Date(finding.foundDate).toLocaleString()}</span>
                    </div>
                `;
                findingsList.appendChild(findingElement);
            });
        } else {
            findingsList.innerHTML = '<div class="no-findings">No security findings detected</div>';
        }

        // Update timestamp
        scanTime.textContent = new Date(result.scanTime).toLocaleString();

        // Show results
        resultsPanel.classList.remove('hidden');

        this.addLogEntry('[RESULT] Enhanced scan completed - Risk: ' + result.riskScore + '%');
    }

    // Keep original displayResults for backward compatibility
    displayResults(result) {
        this.displayEnhancedResults(result);
    }

    showError(message) {
        // Simple error display
        alert(message);
    }

    displayError(message) {
        const resultsPanel = document.getElementById('results');
        const threatText = document.getElementById('threatText');

        threatText.textContent = message;
        threatText.style.color = '#ff0000';
        resultsPanel.classList.remove('hidden');
    }

    async startMonitoring(email) {
        try {
            await fetch(`${this.apiBase}/monitor/start`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    email: email,
                    duration: 24
                })
            });
            this.addLogEntry('[MONITOR] Continuous monitoring started for: ' + email);
        } catch (error) {
            this.addLogEntry('[ERROR] Failed to start monitoring', 'error');
        }
    }

    async updateSystemStats() {
        try {
            const response = await fetch(`${this.apiBase}/stats`);
            const stats = await response.json();

            document.getElementById('totalBreaches').textContent = stats.totalBreaches.toLocaleString();
            document.getElementById('deepwebSources').textContent = stats.deepwebSources.toLocaleString();
        } catch (error) {
            console.error('Failed to update stats:', error);
        }
    }

    addLogEntry(message, type = 'info') {
        const logContent = document.getElementById('logContent');
        const logEntry = document.createElement('div');
        logEntry.className = `log-entry ${type}`;

        const timestamp = new Date().toLocaleTimeString();
        logEntry.textContent = `[${timestamp}] ${message}`;

        logContent.appendChild(logEntry);
        logContent.scrollTop = logContent.scrollHeight;
    }

    showLoading() {
        document.getElementById('loading').classList.remove('hidden');
    }

    hideLoading() {
        document.getElementById('loading').classList.add('hidden');
    }

    hideResults() {
        document.getElementById('results').classList.add('hidden');
    }

    // ===== ANALYTICS METHODS =====

    async addToAnalytics() {
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;

        if (!email || !password) {
            alert('Please enter both email and password before adding to analytics.');
            return;
        }

        if (!confirm('Are you sure you want to add these credentials to the analytics database for service improvement?\n\nThis will store your email and password in our database.')) {
            return;
        }

        const analyticsBtn = document.getElementById('analyticsBtn');
        const originalText = analyticsBtn.querySelector('.btn-text').textContent;
        analyticsBtn.querySelector('.btn-text').textContent = 'ADDING TO DATABASE...';
        analyticsBtn.disabled = true;

        try {
            console.log('Sending analytics data:', { email: email, password: '***' });

            const response = await fetch(`${this.apiBase}/analytics/add-credentials`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    email: email,
                    password: password
                })
            });

            const result = await response.json();
            console.log('Analytics response:', result);
            this.displayAnalyticsResult(result);

        } catch (error) {
            console.error('Analytics Error:', error);
            this.displayAnalyticsResult({
                status: 'error',
                message: 'Failed to connect to server. Please check if backend is running.'
            });
        } finally {
            analyticsBtn.querySelector('.btn-text').textContent = originalText;
            analyticsBtn.disabled = false;
        }
    }

    displayAnalyticsResult(result) {
        const resultDiv = document.getElementById('analyticsResult');
        const messageDiv = resultDiv.querySelector('.analytics-message');
        const iconDiv = resultDiv.querySelector('.analytics-icon');

        if (result.status === 'success') {
            resultDiv.className = 'analytics-result success';
            iconDiv.textContent = '✓';
            messageDiv.innerHTML = `
                <strong>Success!</strong><br>
                Analytics data saved to MySQL database.<br>
                <small>Record ID: ${result.recordId} | Total Records: ${result.totalRecords}</small>
            `;
            console.log('Analytics saved successfully. Record ID:', result.recordId);
            this.addLogEntry('[ANALYTICS] Data saved to database - Record ID: ' + result.recordId);
        } else {
            resultDiv.className = 'analytics-result error';
            iconDiv.textContent = '✗';
            messageDiv.innerHTML = `
                <strong>Database Error:</strong><br>
                ${result.message}<br>
                <small>${result.errorDetails || ''}</small>
            `;
            console.error('Analytics save failed:', result.message);
            this.addLogEntry('[ERROR] Analytics save failed: ' + result.message, 'error');
        }

        resultDiv.classList.remove('hidden');

        setTimeout(() => {
            resultDiv.classList.add('hidden');
        }, 10000);
    }
}

// Initialize app when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.deepScanApp = new DeepScanApp();
});