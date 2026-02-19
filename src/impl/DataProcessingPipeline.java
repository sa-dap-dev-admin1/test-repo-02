package com.blueoptima.connectors.scr.services;

import com.blueoptima.connectors.scr.RevisionRouter;
import com.blueoptima.connectors.scr.common.BOpSCRData;

public class DataProcessingPipeline {
    private final RevisionRouter revisionRouter;
    private final RevisionProcessingService revisionProcessingService;
    private final PullRequestService pullRequestService;
    private final FullCheckoutService fullCheckoutService;
    private final SecretsDetectionService secretsDetectionService;
    private final VulnerabilityDetectionService vulnerabilityDetectionService;
    private final DataPersistenceService dataPersistenceService;

    public DataProcessingPipeline() {
        this.revisionRouter = new RevisionRouter();
        this.revisionProcessingService = new RevisionProcessingService();
        this.pullRequestService = new PullRequestService();
        this.fullCheckoutService = new FullCheckoutService();
        this.secretsDetectionService = new SecretsDetectionService();
        this.vulnerabilityDetectionService = new VulnerabilityDetectionService();
        this.dataPersistenceService = new DataPersistenceService();
    }

    public void processSCRData(BOpSCRData scrData) {
        revisionProcessingService.processRevisions(scrData);
        pullRequestService.processPullRequests(scrData);
        fullCheckoutService.performFullCheckout(scrData);
        secretsDetectionService.detectSecrets(scrData);
        vulnerabilityDetectionService.detectVulnerabilities(scrData);
        dataPersistenceService.persistData(scrData);
    }

    public RevisionRouter getRevisionRouter() {
        return revisionRouter;
    }
}