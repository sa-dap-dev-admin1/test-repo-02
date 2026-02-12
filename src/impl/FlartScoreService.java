package com.blueoptima.uix.service;

import com.blueoptima.uix.exception.FatalPluginProcessingException;
import com.blueoptima.uix.exception.PluginProcessingErrorCode;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class FlartScoreService {

    private final Logger logger;

    public FlartScoreService(Logger logger) {
        this.logger = logger;
    }

    public void setFlartScoreResponses(String requestID, List<FlartScoreRequest> flartScoreRequests,
                                       List<PluginFileMetadata> output) throws FatalPluginProcessingException {
        List<FlartScoreResponse> scores = getFlartScoreResponses(requestID, flartScoreRequests);
        Map<String, FlartScoreResponse> fileToScoreMap = FlartResponseMapper.mapResponsesToPath(flartScoreRequests, scores);
        for (int i = 0; i < scores.size(); i++) {
            PluginFileMetadata currFileMetadata = output.get(i);
            String txWorkingFile = currFileMetadata.getFileInfos().getTxWorkingFile();
            currFileMetadata.setFlartScoreResponse(fileToScoreMap.getOrDefault(txWorkingFile, null));
        }
    }

    public List<FlartScoreResponse> getFlartScoreResponses(String requestID,
                                                           List<FlartScoreRequest> flartScoreRequests) throws FatalPluginProcessingException {
        if (flartScoreRequests.isEmpty()) {
            logger.info("No files to process after metric aggregation for requestID {}", requestID);
            return Collections.emptyList();
        }
        try {
            List<FlartScoreResponse> scores = CommonsUtil.calculateFlartScoreForList(flartScoreRequests, logger);
            if (scores == null || scores.isEmpty()) {
                logger.error("Could not fetch Scores for requestID {} from CEQ ", requestID);
                throw new FatalPluginProcessingException(PluginProcessingErrorCode.FLART_SCORE_API_FAILED);
            }
            return scores;
        } catch (Exception e) {
            logger.error("Could not fetch Scores for requestID {} ", requestID);
            throw new FatalPluginProcessingException(PluginProcessingErrorCode.FLART_SCORE_API_FAILED);
        }
    }
}