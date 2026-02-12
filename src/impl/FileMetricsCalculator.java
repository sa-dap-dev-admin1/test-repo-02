package com.blueoptima.uix.metrics;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class FileMetricsCalculator {

    public static class FileMetrics {
        private final Double maintainabilityScore;
        private final Double scoreChange;
        private final Integer effortSaved;
        private final Double expectedImpact;
        private final String fixesApplied;

        private FileMetrics(Builder builder) {
            this.maintainabilityScore = builder.maintainabilityScore;
            this.scoreChange = builder.scoreChange;
            this.effortSaved = builder.effortSaved;
            this.expectedImpact = builder.expectedImpact;
            this.fixesApplied = builder.fixesApplied;
        }

        public static class Builder {
            private Double maintainabilityScore;
            private Double scoreChange;
            private Integer effortSaved;
            private Double expectedImpact;
            private String fixesApplied;

            public Builder maintainabilityScore(Double maintainabilityScore) {
                this.maintainabilityScore = maintainabilityScore;
                return this;
            }

            public Builder scoreChange(Double scoreChange) {
                this.scoreChange = scoreChange;
                return this;
            }

            public Builder effortSaved(Integer effortSaved) {
                this.effortSaved = effortSaved;
                return this;
            }

            public Builder expectedImpact(Double expectedImpact) {
                this.expectedImpact = expectedImpact;
                return this;
            }

            public Builder fixesApplied(String fixesApplied) {
                this.fixesApplied = fixesApplied;
                return this;
            }

            public FileMetrics build() {
                return new FileMetrics(this);
            }
        }
    }

    public FileMetrics calculateFileMetrics(FlartScoreResponse flartScoreResponse, String fixApplied,
                                            FileAnalyticsResponse analyticsResponse, Logger logger, String fileName, String requestID) {
        Double preScore = 0.0;
        Double postScore = 0.0;
        Integer rawEffort = 0;

        if (analyticsResponse != null) {
            ResponseOriginalFileDataDto flartMetrics = getFlartMetrics(analyticsResponse, logger, fileName, requestID);
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
            return createDefaultFileMetrics(fixApplied);
        }

        return calculateMetrics(preScore, postScore, rawEffort, fixApplied, logger, fileName, requestID);
    }

    private ResponseOriginalFileDataDto getFlartMetrics(FileAnalyticsResponse analyticsResponse, Logger logger, String fileName, String requestID) {
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

    private FileMetrics calculateMetrics(Double preScore, Double postScore, Integer rawEffort, String fixApplied, Logger logger, String fileName, String requestID) {
        double postMaintScore = (postScore != null) ? (1.0 - postScore) * 100.0 : 0.0;
        double preMaintScore = (preScore != null) ? (1.0 - preScore) * 100.0 : 0.0;
        Double maintPctChange = postMaintScore - preMaintScore;
        double expectedImpact = 100 - postMaintScore;
        int effortSaved = (rawEffort != null) ? rawEffort : 0;

        if (maintPctChange < 0.0) {
            logger.warn("maintPctChange is negative for file {} in reqID {}", fileName, requestID);
        }

        return new FileMetrics.Builder()
                .maintainabilityScore(postMaintScore)
                .scoreChange(maintPctChange)
                .effortSaved(effortSaved)
                .expectedImpact(expectedImpact)
                .fixesApplied(fixApplied != null ? fixApplied : "")
                .build();
    }

    private FileMetrics createDefaultFileMetrics(String fixApplied) {
        return new FileMetrics.Builder()
                .maintainabilityScore(0.0)
                .scoreChange(0.0)
                .effortSaved(0)
                .expectedImpact(0.0)
                .fixesApplied(fixApplied != null ? fixApplied : "")
                .build();
    }
}