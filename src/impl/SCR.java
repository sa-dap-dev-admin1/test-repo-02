package com.blueoptima.connectors.scr;

import com.blueoptima.connectors.BOpInfraExtractor;
import com.blueoptima.connectors.common.Housekeeper;
import com.blueoptima.connectors.common.message.RequestDetails;
import com.blueoptima.connectors.common.workflow.ExtractionState;
import com.blueoptima.connectors.scr.orchestrator.SCROrchestrator;
import com.blueoptima.connectors.scr.extractors.ExtractorFactory;
import com.blueoptima.connectors.scr.managers.*;

import java.util.List;

public class SCR extends BOpInfraExtractor {
    private final SCROrchestrator orchestrator;

    public SCR(RequestDetails request, Housekeeper housekeeper, List<ExtractionState> workflow) throws Exception {
        super(request, housekeeper, workflow);
        
        ExtractorFactory extractorFactory = new ExtractorFactory();
        AuthenticationManager authManager = new AuthenticationManager();
        ConfigurationManager configManager = new ConfigurationManager(request);
        ExtractionStateManager stateManager = new ExtractionStateManager(workflow);
        DataPersistenceManager dataManager = new DataPersistenceManager();
        
        this.orchestrator = new SCROrchestrator(
            extractorFactory,
            new MetricsManager(),
            new RevisionManager(),
            new PullRequestManager(),
            authManager,
            configManager,
            stateManager,
            dataManager,
            request,
            housekeeper,
            extractionLog
        );
    }

    @Override
    public void init(String dir) throws Exception {
        orchestrator.init(dir);
    }

    @Override
    public boolean validateRequest() throws Exception {
        return orchestrator.validateRequest();
    }

    @Override
    public BOpExtractionData extract() throws Exception {
        return orchestrator.extract();
    }

    @Override
    public void disconnect() {
        orchestrator.disconnect();
    }

    // Getters for various components if needed
    public SCROrchestrator getOrchestrator() {
        return orchestrator;
    }
}