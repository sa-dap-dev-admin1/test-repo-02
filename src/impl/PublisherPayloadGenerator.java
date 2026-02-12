package com.blueoptima.uix.publisher;

import com.blueoptima.uix.dto.BOpSCRData;
import com.blueoptima.uix.dto.FileToBePublished;
import com.blueoptima.uix.dto.PublisherPayload;
import com.blueoptima.uix.dto.RequestDetails;
import com.blueoptima.uix.exception.FatalPluginProcessingException;
import com.blueoptima.uix.exception.PluginProcessingErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class PublisherPayloadGenerator {

    private static final Logger logger = LoggerFactory.getLogger(PublisherPayloadGenerator.class);

    public static PublisherPayload getFailurePayload(RequestDetails request, BOpSCRData scrData,
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

    private static PublisherPayload enrichAndCreatePayload(RequestDetails request, BOpSCRData scrData,
                                                           boolean isExcluded, int prSize, List<FileToBePublished> filesPublishList) throws FatalPluginProcessingException {
        if (request == null || scrData == null) {
            logger.error("Request or SCR data is null");
            throw new FatalPluginProcessingException(PluginProcessingErrorCode.INVALID_INPUT);
        }

        PublisherPayload.PublisherPayloadBuilder payloadBuilder = PublisherPayload.builder()
                .requestID(request.getRequestID())
                .pr_excluded(isExcluded)
                .prSize(prSize)
                .repositoryName(scrData.getRepositoryName())
                .branchName(scrData.getBranchName())
                .commitID(scrData.getCommitID())
                .scmProvider(scrData.getScmProvider());

        if (filesPublishList != null && !filesPublishList.isEmpty()) {
            List<PublisherPayload.FilePublisherPayload> filePayloads = filesPublishList.stream()
                    .map(PublisherPayloadGenerator::createFilePublisherPayload)
                    .collect(Collectors.toList());
            payloadBuilder.files(filePayloads);
        }

        return payloadBuilder.build();
    }

    private static PublisherPayload.FilePublisherPayload createFilePublisherPayload(FileToBePublished file) {
        return PublisherPayload.FilePublisherPayload.builder()
                .fileName(file.getFileName())
                .filePath(file.getFilePath())
                .preScore(file.getPreScore())
                .postScore(file.getPostScore())
                .effortSaved(file.getEffortSaved())
                .expectedImpact(file.getExpectedImpact())
                .fixesApplied(file.getFixesApplied())
                .build();
    }
}