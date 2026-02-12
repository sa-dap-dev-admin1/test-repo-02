package com.blueoptima.connectors.scr;

import com.blueoptima.connectors.BOpInfraExtractor;
import com.blueoptima.connectors.common.Housekeeper;
import com.blueoptima.connectors.common.message.BOpExtractionData;
import com.blueoptima.connectors.common.message.RequestDetails;
import com.blueoptima.connectors.common.workflow.ExtractionState;
import com.blueoptima.connectors.common.error.BOpException;
import com.blueoptima.connectors.scr.common.BOpSCRData;
import com.blueoptima.connectors.extraction.status.tracker.ScrExtractionTracker;

import java.util.List;

public class SCR extends BOpInfraExtractor {
    private final BOpSCRExtractor extractor;
    private final MetricsGenerationHandler metricsGenerationHandler;
    private final RevisionProcessor revisionProcessor;
    private final ExtractionConfigManager configManager;
    private final ExtractionStateManager stateManager;
    private final AuthenticationHandler authHandler;
    private final ExtractionLogger extractionLogger;
    private final ExtractionExceptionHandler exceptionHandler;

    private BOpSCRData scrData = new BOpSCRData();

    public SCR(RequestDetails request, Housekeeper housekeeper, List<ExtractionState> workflow) throws Exception {
        super(request, housekeeper, workflow);
        
        this.extractor = ExtractorFactory.createExtractor(request, this);
        this.metricsGenerationHandler = new MetricsGenerationHandler(extractor, currentUpdateDir, extractionLog, request);
        this.revisionProcessor = new RevisionProcessor(extractor, currentUpdateDir, request);
        this.configManager = new ExtractionConfigManager(request);
        this.stateManager = new ExtractionStateManager(workflow);
        this.authHandler = new AuthenticationHandler(request.getInfraDetails());
        this.extractionLogger = new ExtractionLogger(currentLogsDir, request.getRequestID());
        this.exceptionHandler = new ExtractionExceptionHandler(this);

        this.statusTracker = new ScrExtractionTracker(this);
    }

    @Override
    public BOpExtractionData extract() throws Exception {
        try {
            for (ExtractionState state : stateManager.getWorkflow()) {
                status = state.getValue();
                stateManager.executeState(state, this);
            }
            return createExtractionData();
        } catch (BOpException e) {
            return exceptionHandler.handleException(e);
        }
    }

    @Override
    public void disconnect() {
        if (extractor != null) {
            extractor.cleanup();
            extractionLogger.info("Disconnecting.....");
            extractor.disconnect();
        }
    }

    // Getters for components
    public BOpSCRExtractor getScrExtractor() {
        return extractor;
    }

    public MetricsGenerationHandler getMetricsGenerationHandler() {
        return metricsGenerationHandler;
    }

    public RevisionProcessor getRevisionProcessor() {
        return revisionProcessor;
    }

    public ExtractionConfigManager getConfigManager() {
        return configManager;
    }

    public ExtractionStateManager getStateManager() {
        return stateManager;
    }

    public AuthenticationHandler getAuthHandler() {
        return authHandler;
    }

    public ExtractionLogger getExtractionLogger() {
        return extractionLogger;
    }

    public BOpSCRData getScrData() {
        return scrData;
    }

    public void setScrData(BOpSCRData scrData) {
        this.scrData = scrData;
    }

    private BOpExtractionData createExtractionData() {
        BOpExtractionData bOpExtractionData = new BOpExtractionData();
        bOpExtractionData.setRequestID(requestID);
        bOpExtractionData.setFileName(currentExtractionDataFile);
        if (request.getRequestType().equals(RequestType.EXTRACTION)) {
            bOpExtractionData.computeIfEmpty(scrData);
        }
        return bOpExtractionData;
    }
}