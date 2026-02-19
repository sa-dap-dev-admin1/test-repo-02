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
    private final ExtractionOrchestrator orchestrator;
    private final InfrastructureFactory infrastructureFactory;
    private BOpSCRData scrData;

    public SCR(RequestDetails request, Housekeeper housekeeper, List<ExtractionState> workflow) throws Exception {
        super(request, housekeeper, workflow);
        this.infrastructureFactory = new InfrastructureFactory();
        this.orchestrator = new ExtractionOrchestrator(this, request, infrastructureFactory);
        this.scrData = new BOpSCRData();
        this.statusTracker = new ScrExtractionTracker(this);
    }

    @Override
    public void init(String dir) throws IllegalAccessException, BOpException {
        orchestrator.initializeExtraction(dir);
    }

    @Override
    public BOpExtractionData extract() throws Exception {
        return orchestrator.executeExtractionWorkflow(scrData);
    }

    @Override
    public void disconnect() {
        orchestrator.disconnect();
    }

    public BOpSCRExtractor getScrExtractor() {
        return orchestrator.getScrExtractor();
    }

    public BOpMetricsGenerator getMetricsEngine() {
        return orchestrator.getMetricsEngine();
    }

    public RevisionRouter getRevisionRouter() {
        return orchestrator.getRevisionRouter();
    }
}