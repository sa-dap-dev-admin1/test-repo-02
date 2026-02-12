package com.blueoptima.uix.service;

import com.blueoptima.uix.dto.BOpSCRData;
import com.blueoptima.uix.dto.FileToBePublished;
import com.blueoptima.uix.dto.PublisherPayload;
import com.blueoptima.uix.dto.RequestDetails;
import com.blueoptima.uix.exception.FatalPluginProcessingException;
import com.blueoptima.uix.exception.PluginProcessingErrorCode;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PublisherService {

    private final Logger logger;

    public PublisherService(Logger logger) {
        this.logger = logger;
    }

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

    private PublisherPayload enrichAndCreatePayload(RequestDetails request, BOpSCRData scrData,
                                                    boolean isFailure, int prSize, List<FileToBePublished> filesPublishList) throws FatalPluginProcessingException {
        if (request == null || scrData == null) {
            throw new FatalPluginProcessingException(PluginProcessingErrorCode.INVALID_INPUT);
        }

        PublisherPayload.PublisherPayloadBuilder payloadBuilder = PublisherPayload.builder()
                .requestID(request.getRequestID())
                .pr_excluded(isFailure)
                .prSize(prSize)
                .repoName(scrData.getRepoName())
                .prNumber(scrData.getPrNumber())
                .prTitle(scrData.getPrTitle())
                .prAuthor(scrData.getPrAuthor())
                .prCreatedAt(scrData.getPrCreatedAt())
                .prMergedAt(scrData.getPrMergedAt())
                .prClosedAt(scrData.getPrClosedAt())
                .prState(scrData.getPrState());

        if (filesPublishList != null && !filesPublishList.isEmpty()) {
            List<PublisherPayload.FileDetails> fileDetailsList = filesPublishList.stream()
                    .map(this::mapToFileDetails)
                    .collect(Collectors.toList());
            payloadBuilder.fileDetails(fileDetailsList);
        }

        return payloadBuilder.build();
    }

    private PublisherPayload.FileDetails mapToFileDetails(FileToBePublished file) {
        return PublisherPayload.FileDetails.builder()
                .fileName(file.getFileName())
                .filePath(file.getFilePath())
                .maintainabilityScore(file.getMaintainabilityScore())
                .scoreChange(file.getScoreChange())
                .effortSaved(file.getEffortSaved())
                .expectedImpact(file.getExpectedImpact())
                .fixesApplied(file.getFixesApplied())
                .build();
    }
}