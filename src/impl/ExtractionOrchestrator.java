package com.blueoptima.connectors.scr;

import com.blueoptima.connectors.common.message.BOpExtractionData;
import com.blueoptima.connectors.common.message.RequestDetails;
import com.blueoptima.connectors.common.error.BOpException;
import com.blueoptima.connectors.scr.common.BOpSCRData;
import com.blueoptima.connectors.scr.services.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ExtractionOrchestrator {
    private static final Logger logger = LogManager.getLogger(ExtractionOrchestrator.class);

    private final SCR scr;
    private final RequestDetails request;
    private final InfrastructureFactory infrastructureFactory;
    private final AuthenticationService authService;
    private final DataProcessingPipeline dataPipeline;
    private final MetricsGenerationService metricsService;
    private final ConfigurationManager configManager;
    private final LoggingService loggingService;

    public ExtractionOrchestrator(SCR scr, RequestDetails request, InfrastructureFactory infrastructureFactory) {
        this.scr = scr;
        this.request = request;
        this.infrastructureFactory = infrastructureFactory;
        this.authService = new AuthenticationService();
        this.dataPipeline = new DataProcessingPipeline();
        this.metricsService = new MetricsGenerationService();
        this.configManager = new ConfigurationManager();
        this.loggingService = new LoggingService();
    }

    public void initializeExtraction(String dir) throws IllegalAccessException, BOpException {
        // Initialize extraction directories and logs
        // This method would contain the logic from the original init method
    }

    public BOpExtractionData executeExtractionWorkflow(BOpSCRData scrData) throws Exception {
        try {
            validateRequest();
            BOpSCRExtractor extractor = infrastructureFactory.createExtractor(request);
            
            authService.authenticate(extractor, request);
            
            List<ExtractionState> workflow = configManager.getWorkflow(request);
            for (ExtractionState state : workflow) {
                executeExtractionState(state, extractor, scrData);
            }
            
            return createExtractionData(scrData);
        } catch (BOpException e) {
            return handleExtractionError(e);
        }
    }

    private void validateRequest() throws BOpException {
        // Implement request validation logic
    }

    private void executeExtractionState(ExtractionState state, BOpSCRExtractor extractor, BOpSCRData scrData) throws Exception {
        // Implement the logic for each extraction state
        // This would replace the large switch statement in the original extract method
    }

    private BOpExtractionData createExtractionData(BOpSCRData scrData) {
        // Create and return BOpExtractionData based on scrData
        return new BOpExtractionData();
    }

    private BOpExtractionData handleExtractionError(BOpException e) throws BOpException {
        // Implement error handling and potential retry logic
        return null;
    }

    public void disconnect() {
        // Implement disconnection logic
    }

    public BOpSCRExtractor getScrExtractor() {
        return infrastructureFactory.getExtractor();
    }

    public BOpMetricsGenerator getMetricsEngine() {
        return metricsService.getMetricsGenerator();
    }

    public RevisionRouter getRevisionRouter() {
        return dataPipeline.getRevisionRouter();
    }
}