package com.blueoptima.connectors.scr;

import com.blueoptima.connectors.common.message.RequestDetails;
import com.blueoptima.connectors.scr.common.BOpSCRData;
import com.blueoptima.connectors.scr.common.RevisionInfo;
import com.blueoptima.connectors.scr.common.RevisionFileMapping;
import com.blueoptima.connectors.scr.common.FileInfo;
import com.blueoptima.connectors.common.error.BOpException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RevisionProcessor {
    private static final Logger logger = LogManager.getLogger(RevisionProcessor.class);

    private final BOpSCRExtractor extractor;
    private final String currentUpdateDir;
    private final RequestDetails request;

    public RevisionProcessor(BOpSCRExtractor extractor, String currentUpdateDir, RequestDetails request) {
        this.extractor = extractor;
        this.currentUpdateDir = currentUpdateDir;
        this.request = request;
    }

    public void processSCRData(BOpSCRData scrData, FullCheckoutConfig fullCheckoutConfig) throws Exception {
        logger.info("Starting SCR data processing");

        processRevisions(scrData);
        processFileInfo(scrData);
        processRevisionFileMappings(scrData);

        if (fullCheckoutConfig != null && fullCheckoutConfig.isFullCheckout()) {
            processFullCheckout(scrData, fullCheckoutConfig);
        }

        logger.info("SCR data processing completed");
    }

    private void processRevisions(BOpSCRData scrData) throws BOpException {
        logger.info("Processing revisions");
        List<RevisionInfo> revisions = extractor.getRevisionData(request.getStartDate().getTime(), request.getEndDate().getTime());
        scrData.setRevisionsInfo(revisions);
        logger.info("Processed {} revisions", revisions.size());
    }

    private void processFileInfo(BOpSCRData scrData) throws BOpException {
        logger.info("Processing file information");
        List<FileInfo> fileInfoList = extractor.getFileInfo(currentUpdateDir);
        scrData.setFilesInfo(fileInfoList);
        logger.info("Processed {} files", fileInfoList.size());
    }

    private void processRevisionFileMappings(BOpSCRData scrData) throws BOpException {
        logger.info("Processing revision file mappings");
        List<RevisionFileMapping> mappings = extractor.getRevisionFileMappings(scrData.getRevisionsInfo(), scrData.getFilesInfo());
        scrData.setRevisionFileMappings(mappings);
        logger.info("Processed {} revision file mappings", mappings.size());
    }

    private void processFullCheckout(BOpSCRData scrData, FullCheckoutConfig fullCheckoutConfig) throws BOpException {
        logger.info("Processing full checkout");
        String fullCheckoutPath = fullCheckoutConfig.getFullCheckoutPath();
        
        Map<String, RevisionInfo> revisionInfoMap = scrData.getRevisionsInfo().stream()
                .collect(Collectors.toMap(RevisionInfo::getRevision, r -> r));

        List<RevisionFileMapping> fullCheckoutMappings = extractor.getFullCheckoutRevisionFileMappings(fullCheckoutPath, revisionInfoMap);
        scrData.setRevisionFileMappings(fullCheckoutMappings);

        List<FileInfo> fullCheckoutFileInfo = extractor.getFullCheckoutFileInfo(fullCheckoutPath);
        scrData.setFilesInfo(fullCheckoutFileInfo);

        logger.info("Full checkout processing completed. Mappings: {}, Files: {}", fullCheckoutMappings.size(), fullCheckoutFileInfo.size());
    }
}