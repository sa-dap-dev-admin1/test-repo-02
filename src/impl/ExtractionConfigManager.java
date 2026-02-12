package com.blueoptima.connectors.scr;

import com.blueoptima.connectors.common.message.RequestDetails;
import com.blueoptima.connectors.util.ConfigUtil;
import com.blueoptima.connectors.IntegratorConfigTracker;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Map;
import java.util.Optional;

public class ExtractionConfigManager {
    private static final Logger logger = LogManager.getLogger(ExtractionConfigManager.class);

    private final RequestDetails request;
    private final IntegratorConfigTracker configTracker;

    public ExtractionConfigManager(RequestDetails request) {
        this.request = request;
        this.configTracker = IntegratorConfigTracker.instance();
    }

    public boolean isSharedDirectoryEnabled() {
        return getBooleanConfig("SHARED_DIRECTORY_ENABLED", false);
    }

    public Map<String, Object> getRequestConfig() {
        return request.getRequestConfig();
    }

    public boolean isDebugEnabled() {
        return getBooleanConfig("debug", false);
    }

    public int getRevisionActivityThreshold() {
        return getIntConfig("REVISION_ACTIVITY_THRESHOLD", 1000);
    }

    public boolean isMetricsEnabled() {
        return getBooleanConfig("METRICS_ENABLED", true);
    }

    public boolean isBuildFileRevisionBranchAssociationEnabled() {
        return getBooleanConfig("BUILD_FILE_REVISION_BRANCH_ASSOCIATION", false);
    }

    public boolean isProcessPRDeleteEnabled() {
        return getBooleanConfig("PROCESS_PR_DELETE", false);
    }

    public String getFileTypes() {
        return getStringConfig("fileTypes", null);
    }

    private boolean getBooleanConfig(String key, boolean defaultValue) {
        Optional<Boolean> requestValue = Optional.ofNullable(getRequestConfig())
                .map(config -> (String) config.get(key))
                .map(Boolean::parseBoolean);

        if (requestValue.isPresent()) {
            return requestValue.get();
        }

        return configTracker.getBooleanConfig(key, defaultValue);
    }

    private int getIntConfig(String key, int defaultValue) {
        Optional<Integer> requestValue = Optional.ofNullable(getRequestConfig())
                .map(config -> (String) config.get(key))
                .map(Integer::parseInt);

        if (requestValue.isPresent()) {
            return requestValue.get();
        }

        return configTracker.getValueFromConfig(key, defaultValue, logger);
    }

    private String getStringConfig(String key, String defaultValue) {
        return Optional.ofNullable(getRequestConfig())
                .map(config -> (String) config.get(key))
                .orElse(defaultValue);
    }
}