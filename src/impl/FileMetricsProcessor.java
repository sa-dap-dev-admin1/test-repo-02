package com.blueoptima.uix.controller;

import com.blueoptima.uix.dto.FileAnalyticsResponse;
import com.blueoptima.uix.dto.FlartDataDto;
import com.blueoptima.uix.dto.FlartScoreRequest;
import com.blueoptima.uix.dto.FlartScoreResponse;
import com.blueoptima.uix.dto.PluginFileMetadata;
import com.blueoptima.uix.dto.ResponseOriginalFileDataDto;
import com.blueoptima.uix.dto.ResponseOriginalFileDto;
import com.blueoptima.uix.exception.FatalPluginProcessingException;
import com.blueoptima.uix.exception.PluginFileProcessingException;
import com.blueoptima.uix.exception.PluginProcessingErrorCode;
import com.blueoptima.uix.util.CommonsUtil;
import com.blueoptima.uix.util.FlartResponseMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class FileMetricsProcessor {

    private static final Logger logger = LoggerFactory.getLogger(FileMetricsProcessor.class);

    public static class FileMetrics {
        private Double maintainability_score;
        private Double score_change;
        private Integer effort_saved;
        private Double expected_impact;
        private String fixes_applied;

        public FileMetrics(Double maintainability_score, Double score_change, Integer effort_saved, Double expected_impact, String fixes_applied) {
            this.maintainability_score = maintainability_score;
            this.score_change = score_change;
            this.effort_saved = effort_saved;
            this.fixes_applied = fixes_applied;
            this.expected_impact = expected_impact;
        }
    }

    public FileMetrics getFileAnalysis(FlartScoreResponse flartScoreResponse, String fixApplied,
                                       FileAnalyticsResponse analyticsResponse, String fileName, String requestID)
            throws PluginFileProcessingException {

        Double preScore = 0.0;
        Double postScore = 0.0;
        Integer rawEffort = 0;

        if (analyticsResponse != null) {
            logger.info("Reading new analytics response for file {} in reqID {}", fileName, requestID);
            ResponseOriginalFileDataDto flartMetrics = getFlartMetrics(analyticsResponse, fileName, requestID);
            if (flartMetrics != null && flartMetrics.getFlart() != null) {
                FlartDataDto flartMetricsForFile = flartMetrics.getFlart();
                postScore = flartMetricsForFile.getNew();
                preScore = flartMetricsForFile.getOld();
                rawEffort = (flartScoreResponse != null && flartScoreResponse.getEffortToFix() != null) ? flartScoreResponse.getEffortToFix() : 0;
            } else {
                logger.warn("New analytics response is null for file {} in reqID {}", fileName, requestID);
            }
        } else if (flartScoreResponse != null) {
            preScore = flartScoreResponse.getFilePreFlartScore();
            postScore = flartScoreResponse.getFilePostFlartScore();
            rawEffort = flartScoreResponse.getEffortToFix();
        } else {
            logger.warn("FileAnalysis/FlartScoreResponse is null for file {} in reqID {}", fileName, requestID);
            return new FileMetrics(0.0, 0.0, 0, 0.0, fixApplied);
        }

        return getFileMetricsHelper(flartScoreResponse, fixApplied, fileName, preScore, postScore, rawEffort, requestID);
    }

    private ResponseOriginalFileDataDto getFlartMetrics(FileAnalyticsResponse analyticsResponse, String fileName, String requestID) throws PluginFileProcessingException {
        if (analyticsResponse.getOriginalFiles() == null || analyticsResponse.getOriginalFiles().isEmpty()) {
            logger.error("No original files found for file {} in reqID {}", fileName, requestID);
            throw new PluginFileProcessingException(PluginProcessingErrorCode.METRICS_DATA_PROCESSING_FAILED);
        }
        return analyticsResponse.getOriginalFiles()
                .stream()
                .filter(Objects::nonNull)
                .filter(ResponseOriginalFileDto::isPrimary)
                .map(ResponseOriginalFileDto::getResponseOriginalFileData)
                .findFirst()
                .orElse(null);
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

    private List<FlartScoreResponse> getFlartScoreResponses(String requestID,
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

    private FileMetrics getFileMetricsHelper(FlartScoreResponse flartScoreResponse,
                                             String fixApplied, String fileName, Double preScore,
                                             Double postScore, Integer rawEffort, String requestID) {
        double postmaintScore = 0.0;
        double premaintScore = 0.0;
        Double maintPctChange = null;
        double expectedImpact = 0.0;
        int effortSaved = 0;
        String safeFixApplied = (fixApplied != null) ? fixApplied : "";

        if (flartScoreResponse == null) {
            logger.warn("FileAnalysis/FlartScoreResponse is null for file {} in reqID {}", fileName, requestID);
            return new FileMetrics(postmaintScore, maintPctChange, effortSaved, expectedImpact, safeFixApplied);
        }

        effortSaved = (rawEffort != null) ? rawEffort : 0;

        if (postScore != null) {
            postmaintScore = (1.0 - postScore) * 100.0;
            expectedImpact = 100 - postmaintScore;
        }
        if (preScore != null) {
            premaintScore = (1.0 - preScore) * 100.0;
            maintPctChange = postmaintScore - premaintScore;
        }

        if (maintPctChange != null && maintPctChange < 0.0) {
            logger.warn("maintPctChange is negative for file {} in reqID {}", fileName, requestID);
        }
        return new FileMetrics(postmaintScore, maintPctChange, effortSaved, expectedImpact, safeFixApplied);
    }
}