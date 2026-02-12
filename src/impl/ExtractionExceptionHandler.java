package com.blueoptima.connectors.scr;

import com.blueoptima.connectors.common.error.BOpException;
import com.blueoptima.connectors.common.message.BOpExtractionData;

public class ExtractionExceptionHandler {
    private final SCR scr;

    public ExtractionExceptionHandler(SCR scr) {
        this.scr = scr;
    }

    public BOpExtractionData handleException(BOpException e) throws Exception {
        if (scr.shouldRetry && !scr.isExtractionCancelled()) {
            scr.getExtractionLogger().error("Error Occurred in extraction. Retrying again ", e);
            return scr.retryExtraction();
        } else {
            throw e;
        }
    }
}