package com.blueoptima.connectors.scr.orchestrator;

import com.blueoptima.connectors.BOpExtractionData;
import com.blueoptima.connectors.common.Housekeeper;
import com.blueoptima.connectors.common.message.RequestDetails;
import com.blueoptima.connectors.common.error.BOpException;
import com.blueoptima.connectors.scr.extractors.ExtractorFactory;
import com.blueoptima.connectors.scr.managers.*;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class SCROrchestrator {
    private final ExtractorFactory extractorFactory;
    private final MetricsManager metricsManager;
    private final RevisionManager revisionManager;
    private final PullRequestManager pullRequestManager;
    private final AuthenticationManager authManager;
    private final ConfigurationManager configManager;
    private final ExtractionStateManager stateManager;
    private final DataPersistenceManager dataManager;
    private final RequestDetails request;
    private final Housekeeper housekeeper;
    private final Logger extractionLog;

    public SCROrchestrator(
            ExtractorFactory extractorFactory,
            MetricsManager metricsManager,
            RevisionManager revisionManager,
            PullRequestManager pullRequestManager,
            AuthenticationManager authManager,
            ConfigurationManager configManager,
            ExtractionStateManager stateManager,
            DataPersistenceManager dataManager,
            RequestDetails request,
            Housekeeper housekeeper,
            Logger extractionLog) {
        this.extractorFactory = extractorFactory;
        this.metricsManager = metricsManager;
        this.revisionManager = revisionManager;
        this.pullRequestManager = pullRequestManager;
        this.authManager = authManager;
        this.configManager = configManager;
        this.stateManager = stateManager;
        this.dataManager = dataManager;
        this.request = request;
        this.housekeeper = housekeeper;
        this.extractionLog = extractionLog;
    }

    public void init(String dir) throws Exception {
        File dataDir = new File(dir);
        if (!dataDir.exists() && !dataDir.mkdirs()) {
            throw new BOpException("Failed to create directory: " + dir);
        }
        configManager.setDataDir(dir);
        extractionLog.info("Initialized SCR orchestrator with data directory: " + dir);
    }

    public boolean validateRequest() throws Exception {
        return configManager.validateRequest(request);
    }

    public BOpExtractionData extract() throws Exception {
        while (stateManager.hasNextState()) {
            String currentState = stateManager.getCurrentState();
            extractionLog.info("Processing state: " + currentState);
            
            switch (currentState) {
                case "INIT":
                    extractorFactory.createExtractor(request.getInfraDetails().getInfraName());
                    break;
                case "CONNECT":
                    authManager.connect(request);
                    break;
                case "AUTHENTICATE":
                    authManager.authenticate(request);
                    break;
                case "EXTRACT_METADATA":
                    revisionManager.extractMetadata(request);
                    break;
                case "EXTRACT_PR_DATA":
                    pullRequestManager.extractPRData(request);
                    break;
                case "GENERATE_METRICS":
                    metricsManager.generateMetrics(request);
                    break;
                case "SAVE_DATA":
                    dataManager.saveData(request);
                    break;
                case "DISCONNECT":
                    authManager.disconnect();
                    break;
                case "EXTRACTION_DONE":
                    extractionLog.info("Extraction completed successfully");
                    return dataManager.getExtractionData();
                default:
                    extractionLog.warn("Unknown state: " + currentState);
                    break;
            }
            stateManager.moveToNextState();
        }
        throw new BOpException("Extraction process did not complete");
    }

    public void disconnect() {
        authManager.disconnect();
        extractionLog.info("Disconnected from the repository");
    }
}