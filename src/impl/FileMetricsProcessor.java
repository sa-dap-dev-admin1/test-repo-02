package com.blueoptima.uix.processor;

import com.blueoptima.uix.dto.FileAnalyticsResponse;
import com.blueoptima.uix.dto.FlartScoreResponse;
import com.blueoptima.uix.dto.ResponseOriginalFileDataDto;
import com.blueoptima.uix.dto.ResponseOriginalFileDto;
import com.blueoptima.uix.exception.PluginFileProcessingException;
import com.blueoptima.uix.exception.PluginProcessingErrorCode;
import org.slf4j.Logger;

import java.util.Objects;

public class FileMetricsProcessor {

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

    public static FileMetrics getFileAnalysis(FlartScoreResponse flartScoreResponse, String fixApplied,
                                              FileAnalyticsResponse analyticsResponse, Logger logger, String fileName, String requestID)
            throws PluginFileProcessingException {

        Double preScore = 0.0;
        Double postScore = 0.0;
        Integer rawEffort = 0;

        if (analyticsResponse != null) {
            logger.info("Reading new analytics response for file {} in reqID {}", fileName, requestID);
            ResponseOriginalFileDataDto flartMetrics = getFlartMetrics(analyticsResponse, logger, fileName, requestID);
            if (flartMetrics != null && flartMetrics.getFlart() != null) {
                postScore = flartMetrics.getFlart().getNew();
                preScore = flartMetrics.getFlart().getOld();
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

        return getFileMetricsHelper(flartScoreResponse, fixApplied, logger, fileName, preScore, postScore, rawEffort, requestID);
    }

    private static ResponseOriginalFileDataDto getFlartMetrics(FileAnalyticsResponse analyticsResponse, Logger logger, String fileName, String requestID) throws PluginFileProcessingException {
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

    private static FileMetrics getFileMetricsHelper(FlartScoreResponse flartScoreResponse,
                                                    String fixApplied, Logger logger, String fileName, Double preScore,
                                                    Double postScore, Integer rawEffort, String requestID) {
        double postmaintScore = 0.0;
        double premaintScore = 0.0;
        Double maintPctChange = null;
        double expectedImpact = 0.0;
        int effortSaved = rawEffort != null ? rawEffort : 0;
        String safeFixApplied = fixApplied != null ? fixApplied : "";

        if (flartScoreResponse == null) {
            logger.warn("FlartScoreResponse is null for file {} in reqID {}", fileName, requestID);
            return new FileMetrics(postmaintScore, maintPctChange, effortSaved, expectedImpact, safeFixApplied);
        }

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