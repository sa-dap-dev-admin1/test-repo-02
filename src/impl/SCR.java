package com.blueoptima.connectors.scr;

import com.blueoptima.connectors.BOpInfraExtractor;
import com.blueoptima.connectors.common.Housekeeper;
import com.blueoptima.connectors.common.message.BOpExtractionData;
import com.blueoptima.connectors.common.message.RequestDetails;
import com.blueoptima.connectors.common.workflow.ExtractionState;
import com.blueoptima.connectors.extraction.status.tracker.ScrExtractionTracker;
import com.blueoptima.connectors.scr.managers.*;
import com.blueoptima.connectors.scr.processors.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class SCR extends BOpInfraExtractor {
    private static final Logger logger = LogManager.getLogger(SCR.class);

    private final ExtractionWorkflowManager workflowManager;
    private final ConfigurationManager configManager;
    private final AuthenticationManager authManager;
    private final ExtractionDataWriter dataWriter;
    private final TelemetryManager telemetryManager;

    public SCR(RequestDetails request, Housekeeper housekeeper, List<ExtractionState> workflow) throws Exception {
        super(request, housekeeper, workflow);
        
        this.configManager = new ConfigurationManager(request);
        this.authManager = new AuthenticationManager(configManager);
        this.dataWriter = new ExtractionDataWriter(configManager);
        this.telemetryManager = new TelemetryManager();
        
        ExtractionRetryManager retryManager = new ExtractionRetryManager(this);
        GitExtractorManager gitManager = new GitExtractorManager(configManager, authManager);
        ScrDataProcessor dataProcessor = new ScrDataProcessor(configManager);
        PullRequestHandler prHandler = new PullRequestHandler(configManager, authManager);
        FullCheckoutManager checkoutManager = new FullCheckoutManager(configManager, gitManager);
        MetricsGenerator metricsGenerator = new MetricsGenerator(configManager);
        SecretDetectionManager secretManager = new SecretDetectionManager(configManager);
        MachineCodeDetector codeDetector = new MachineCodeDetector(configManager);
        SourceVulnerabilityDetector vulDetector = new SourceVulnerabilityDetector(configManager);
        LLMService llmService = new LLMService(configManager);
        PluginProcessor pluginProcessor = new PluginProcessor(configManager);
        BuildRevisionManager revisionManager = new BuildRevisionManager(configManager, gitManager);

        this.workflowManager = new ExtractionWorkflowManager(
            this, configManager, authManager, gitManager, dataProcessor, prHandler, 
            checkoutManager, metricsGenerator, secretManager, codeDetector, vulDetector, 
            llmService, pluginProcessor, revisionManager, retryManager, dataWriter, telemetryManager
        );

        this.statusTracker = new ScrExtractionTracker(this);
    }

    @Override
    public void init(String dir) throws Exception {
        configManager.initializeDirectories(dir);
        dataWriter.setCurrentExtractionDataFile(configManager.getCurrentExtractionDataFile());
        setExtractionLog(configManager.getLogFilePath(), configManager.getLogLevel());
    }

    @Override
    public BOpExtractionData extract() throws Exception {
        return workflowManager.executeWorkflow();
    }

    @Override
    public void disconnect() {
        workflowManager.cleanup();
    }

    // Getters for various managers and processors if needed
}