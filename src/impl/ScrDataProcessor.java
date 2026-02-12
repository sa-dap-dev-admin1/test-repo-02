package com.blueoptima.connectors.scr.processors;

import com.blueoptima.connectors.scr.managers.ConfigurationManager;
import com.blueoptima.connectors.scr.common.BOpSCRData;
import com.blueoptima.connectors.scr.common.RevisionInfo;
import com.blueoptima.connectors.scr.common.BOpRelease;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Date;

public class ScrDataProcessor {
    private final ConfigurationManager configManager;
    private final Logger logger;

    public ScrDataProcessor(ConfigurationManager configManager) {
        this.configManager = configManager;
        this.logger = configManager.getLogger();
    }

    public void extractMetadata(BOpSCRData scrData) throws Exception {
        extractRevisionData(scrData);
        extractReleaseData(scrData);
    }

    private void extractRevisionData(BOpSCRData scrData) throws Exception {
        logger.info("Extracting revision data");
        List<RevisionInfo> revisions = configManager.getRequest().getInfraExtractor().getRevisionData(
            configManager.getRequest().getStartDate().getTime(),
            configManager.getRequest().getEndDate().getTime()
        );
        scrData.setRevisionsInfo(revisions);
        logger.info("Extracted {} revisions", revisions.size());
    }

    private void extractReleaseData(BOpSCRData scrData) throws Exception {
        logger.info("Extracting release data");
        List<BOpRelease> releases = configManager.getRequest().getInfraExtractor().getReleaseData(
            configManager.getInfraDetails().getInstanName(),
            configManager.getRequest().getStartDate().getTime(),
            configManager.getRequest().getEndDate().getTime()
        );
        scrData.setReleases(releases);
        logger.info("Extracted {} releases", releases.size());
    }

    public void processSCRData(BOpSCRData scrData) throws Exception {
        logger.info("Processing SCR data");
        // Add any additional processing logic here
        // For example, you might want to filter or transform the data
    }

    public void setSCAEnabledInSCRData(BOpSCRData scrData) {
        boolean scaEnabled = com.blueoptima.sca.SCAUtil.isSCAEnabledForExtractionRequest(configManager.getRequest());
        scrData.setScaEnabledDuringExtraction(scaEnabled);
        logger.info("SCA enabled during extraction: {}", scaEnabled);
    }
}