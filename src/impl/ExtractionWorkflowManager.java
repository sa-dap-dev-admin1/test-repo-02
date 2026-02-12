package com.blueoptima.connectors.scr.managers;

import com.blueoptima.connectors.common.message.BOpExtractionData;
import com.blueoptima.connectors.common.workflow.ExtractionState;
import com.blueoptima.connectors.scr.SCR;
import com.blueoptima.connectors.scr.processors.*;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ExtractionWorkflowManager {
    private final SCR scr;
    private final ConfigurationManager configManager;
    private final AuthenticationManager authManager;
    private final GitExtractorManager gitManager;
    private final ScrDataProcessor dataProcessor;
    private final PullRequestHandler prHandler;
    private final FullCheckoutManager checkoutManager;
    private final MetricsGenerator metricsGenerator;
    private final SecretDetectionManager secretManager;
    private final MachineCodeDetector codeDetector;
    private final SourceVulnerabilityDetector vulDetector;
    private final LLMService llmService;
    private final PluginProcessor pluginProcessor;
    private final BuildRevisionManager revisionManager;
    private final ExtractionRetryManager retryManager;
    private final ExtractionDataWriter dataWriter;
    private final TelemetryManager telemetryManager;

    private final Logger logger;

    public ExtractionWorkflowManager(SCR scr, ConfigurationManager configManager, 
                                     AuthenticationManager authManager, GitExtractorManager gitManager, 
                                     ScrDataProcessor dataProcessor, PullRequestHandler prHandler, 
                                     FullCheckoutManager checkoutManager, MetricsGenerator metricsGenerator, 
                                     SecretDetectionManager secretManager, MachineCodeDetector codeDetector, 
                                     SourceVulnerabilityDetector vulDetector, LLMService llmService, 
                                     PluginProcessor pluginProcessor, BuildRevisionManager revisionManager, 
                                     ExtractionRetryManager retryManager, ExtractionDataWriter dataWriter, 
                                     TelemetryManager telemetryManager) {
        this.scr = scr;
        this.configManager = configManager;
        this.authManager = authManager;
        this.gitManager = gitManager;
        this.dataProcessor = dataProcessor;
        this.prHandler = prHandler;
        this.checkoutManager = checkoutManager;
        this.metricsGenerator = metricsGenerator;
        this.secretManager = secretManager;
        this.codeDetector = codeDetector;
        this.vulDetector = vulDetector;
        this.llmService = llmService;
        this.pluginProcessor = pluginProcessor;
        this.revisionManager = revisionManager;
        this.retryManager = retryManager;
        this.dataWriter = dataWriter;
        this.telemetryManager = telemetryManager;
        
        this.logger = configManager.getLogger();
    }

    public BOpExtractionData executeWorkflow() throws Exception {
        List<ExtractionState> workflow = scr.getWorkflow();
        for (ExtractionState state : workflow) {
            scr.setStatus(state.getValue());
            executeState(state);
        }
        return finalizeExtraction();
    }

    private void executeState(ExtractionState state) throws Exception {
        switch (state.getValue()) {
            case VALIDATE_DETAIL:
                configManager.validateRequest();
                break;
            case INIT:
                gitManager.initialize();
                break;
            case CONNECT:
                gitManager.connect();
                break;
            case AUTHENTICATE:
                authManager.authenticate();
                break;
            case DOWNLOAD_REPOSITORY:
                gitManager.downloadRepository();
                break;
            case EXTRACT_METADATA:
                dataProcessor.extractMetadata();
                break;
            case EXTRACT_PR_DATA:
                prHandler.extractPullRequestData();
                break;
            case BUILD_REVISION_SET:
                revisionManager.buildRevisionSet();
                break;
            case GENERATE_METRICS:
                metricsGenerator.generateMetrics();
                break;
            case EXTRACT_SECRET_DATA_ON_FULL_CHECKOUT:
                secretManager.detectSecrets();
                break;
            case EXTRACT_MLCD_ON_FULL_CHECKOUT:
                codeDetector.detectMachineGeneratedCode();
                break;
            case EXTRACT_SVD_ON_FULL_CHECKOUT:
                vulDetector.detectVulnerabilities();
                break;
            case EXTRACT_HTF_DATA:
                llmService.generateHowToFixSuggestions();
                break;
            case MAINTAINABILITY_APP_EVENTS:
                pluginProcessor.processPluginEvents();
                break;
            case SAVE_DATA_TO_FILE:
                dataWriter.saveDataToFile();
                break;
            // Add other cases as needed
            default:
                logger.warn("Unhandled extraction state: " + state.getValue());
        }
    }

    private BOpExtractionData finalizeExtraction() throws Exception {
        scr.setStatus(ExtractionStatus.EXTRACTION_DONE);
        logger.info("Extraction was successful :-)");
        return dataWriter.createExtractionData();
    }

    public void cleanup() {
        gitManager.disconnect();
        // Add any other cleanup operations
    }
}