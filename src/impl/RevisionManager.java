package com.blueoptima.connectors.scr.managers;

import com.blueoptima.connectors.common.message.RequestDetails;
import com.blueoptima.connectors.scr.BOpSCRExtractor;
import com.blueoptima.connectors.scr.common.RevisionInfo;
import com.blueoptima.connectors.scr.common.BOpSCRData;
import com.blueoptima.connectors.common.error.BOpException;
import java.util.List;

public class RevisionManager {
    private BOpSCRExtractor extractor;
    private BOpSCRData scrData;

    public void setExtractor(BOpSCRExtractor extractor) {
        this.extractor = extractor;
    }

    public void setScrData(BOpSCRData scrData) {
        this.scrData = scrData;
    }

    public void extractMetadata(RequestDetails request) throws BOpException {
        if (extractor == null) {
            throw new BOpException("Extractor not set");
        }
        if (scrData == null) {
            throw new BOpException("SCR data not set");
        }

        try {
            List<RevisionInfo> revisions = extractor.getRevisionData(
                request.getStartDate().getTime(),
                request.getEndDate().getTime()
            );
            scrData.setRevisionsInfo(revisions);
        } catch (Exception e) {
            throw new BOpException("Failed to extract revision metadata", e);
        }
    }

    public BOpSCRData getScrData() {
        return scrData;
    }
}