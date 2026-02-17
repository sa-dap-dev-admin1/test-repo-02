package com.blueoptima.connectors.scr.managers;

import com.blueoptima.connectors.common.message.RequestDetails;
import com.blueoptima.connectors.scr.BOpSCRExtractor;
import com.blueoptima.connectors.scr.common.BOpSCRData;
import com.blueoptima.connectors.pullrequest.PRData;
import com.blueoptima.connectors.common.error.BOpException;

public class PullRequestManager {
    private BOpSCRExtractor extractor;
    private BOpSCRData scrData;

    public void setExtractor(BOpSCRExtractor extractor) {
        this.extractor = extractor;
    }

    public void setScrData(BOpSCRData scrData) {
        this.scrData = scrData;
    }

    public void extractPRData(RequestDetails request) throws BOpException {
        if (extractor == null) {
            throw new BOpException("Extractor not set");
        }
        if (scrData == null) {
            throw new BOpException("SCR data not set");
        }

        try {
            boolean processPRDelete = Boolean.parseBoolean(
                (String) request.getRequestConfig().getOrDefault("PROCESS_PR_DELETE", "false")
            );

            PRData prData = extractor.getPullRequestData(request.getInfraDetails(), request, processPRDelete);
            
            if (prData == null) {
                scrData.setPullRequestList(null);
            } else {
                scrData.setPullRequestList(prData.getPullRequestData());
                
                if (processPRDelete) {
                    // Implement fetchAndUpdateDeletedPRIdList here if needed
                }
            }
        } catch (Exception e) {
            throw new BOpException("Failed to extract pull request data", e);
        }
    }

    public BOpSCRData getScrData() {
        return scrData;
    }
}