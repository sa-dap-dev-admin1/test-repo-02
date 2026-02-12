package com.blueoptima.uix.controller;

import com.blueoptima.uix.dto.BOpSCRData;
import com.blueoptima.uix.dto.FileToBePublished;
import com.blueoptima.uix.dto.PublisherPayload;
import com.blueoptima.uix.dto.RequestDetails;
import com.blueoptima.uix.exception.FatalPluginProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PublisherPayloadGenerator {

    private static final Logger logger = LoggerFactory.getLogger(PublisherPayloadGenerator.class);

    public PublisherPayload getFailurePayload(RequestDetails request, BOpSCRData scrData,
                                              String exclusionReason, int prSize, List<FileToBePublished> filesPublishList) {
        try {
            logger.info("Creating publisher Failure scenario payload for Maintainability Plugin...");
            PublisherPayload details = enrichAndCreatePayload(request, scrData, true, prSize, filesPublishList);
            details.setPrExclusionReason(exclusionReason);
            return details;
        } catch (FatalPluginProcessingException e) {
            logger.error("Error {} in creating publisher Failure scenario payload for Maintainability Plugin for req {}", e.getMessage(), request.getRequestID());
            return PublisherPayload.builder()
                    .requestID(request.getRequestID())
                    .pr_excluded(true)
                    .prExclusionReason(exclusionReason)
                    .build();
        }
    }

    private PublisherPayload enrichAndCreatePayload(RequestDetails request, BOpSCRData scrData, boolean isFailure, int prSize, List<FileToBePublished> filesPublishList) throws FatalPluginProcessingException {
        // Implementation of enrichAndCreatePayload method
        // This method should create and return a PublisherPayload object
        // As the implementation details are not provided in the original code, you would need to implement this based on your specific requirements
        throw new UnsupportedOperationException("Method not implemented");
    }
}