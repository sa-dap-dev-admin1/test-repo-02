package com.blueoptima.connectors.scr;

import com.blueoptima.connectors.common.error.BOpException;
import com.blueoptima.connectors.common.message.BOpExtractionData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SCRExceptionHandler {
    private static final Logger logger = LogManager.getLogger(SCRExceptionHandler.class);

    public BOpExtractionData handleException(BOpException e, SCR scr) throws BOpException {
        logger.error("Exception occurred during extraction", e);

        if (scr.shouldRetry && !scr.isExtractionCancelled()) {
            logger.info("Attempting to retry extraction");
            return scr.retryExtraction();
        } else {
            if (scr.isExtractionCancelled()) {
                logger.info("Extraction was cancelled");
                return null;
            } else {
                throw e;
            }
        }
    }
}