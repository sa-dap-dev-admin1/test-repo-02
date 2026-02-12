package com.blueoptima.connectors.scr;

import com.blueoptima.connectors.common.error.BOpException;
import com.blueoptima.connectors.scr.common.BOpSCRData;
import com.blueoptima.connectors.scr.common.RevisionInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class SCRDataProcessor {
    private static final Logger logger = LogManager.getLogger(SCRDataProcessor.class);
    private final SCRConfiguration configuration;
    private final SCRExtractor extractor;

    public SCRDataProcessor(SCRConfiguration configuration, SCRExtractor extractor) {
        this.configuration = configuration;
        this.extractor = extractor;
    }

    public void extractMetadata(BOpSCRData scrData) throws BOpException {
        try {
            logger.info("Starting metadata extraction");
            List<RevisionInfo> revisions = extractor.getRevisionData(
                configuration.getRequest().getStartDate().getTime(),
                configuration.getRequest().getEndDate().getTime()
            );
            scrData.setRevisionsInfo(revisions);

            List<BOpRelease> releases = extractor.getReleaseData(
                configuration.getRequest().getInfraDetails().getInstanName(),
                configuration.getRequest().getStartDate().getTime(),
                configuration.getRequest().getEndDate().getTime()
            );
            scrData.setReleases(releases);

            logger.info("Metadata extraction completed successfully");
        } catch (Exception e) {
            logger.error("Error during metadata extraction", e);
            throw new BOpException(ExtractionStatus.EXTRACT_METADATA, GenericErrorCode.RCE0001, "Metadata extraction failed", e);
        }
    }
}