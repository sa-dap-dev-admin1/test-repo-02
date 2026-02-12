package com.blueoptima.connectors.scr.managers;

import com.blueoptima.connectors.common.message.RequestDetails;
import com.blueoptima.connectors.common.message.InfrastructureDetails;
import com.blueoptima.connectors.ConnectorConstants;
import com.blueoptima.connectors.common.BOpFileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class ConfigurationManager {
    private final RequestDetails request;
    private final InfrastructureDetails infraDetails;
    private final Logger logger;

    private String currentLogsDir;
    private String currentDataDir;
    private String currentUpdateDir;
    private String currentExtractionDataFile;

    public ConfigurationManager(RequestDetails request) {
        this.request = request;
        this.infraDetails = request.getInfraDetails();
        this.logger = LogManager.getLogger(ConfigurationManager.class);
    }

    public void initializeDirectories(String dir) throws IOException {
        String requestId = request.getRequestID();
        
        currentLogsDir = ConnectorConstants.EXTRACTION_LOGS_DIR;
        currentDataDir = ConnectorConstants.EXTRACTION_DATA_DIR + File.separator + dir + File.separator + requestId;
        currentUpdateDir = currentDataDir + File.separator + ConnectorConstants.DEF_UPDATE_DIR;
        currentExtractionDataFile = currentDataDir + File.separator + "Data_" + requestId + ConnectorConstants.FILE_EXTENSION_SEARIALIZED;

        createAndAuthorizeDirectories();
    }

    private void createAndAuthorizeDirectories() throws IOException {
        createAndAuthorizeDirectory(currentLogsDir);
        createAndAuthorizeDirectory(currentDataDir);
        createAndAuthorizeDirectory(currentUpdateDir);
    }

    private void createAndAuthorizeDirectory(String path) throws IOException {
        File dir = BOpFileUtils.authorizeFilePath(path);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create directory: " + path);
        }
    }

    public void validateRequest() throws Exception {
        if (infraDetails.getInstanUrl() == null || infraDetails.getInstanUrl().isEmpty()) {
            throw new IllegalArgumentException("URL was not defined in the request");
        }

        if (infraDetails.getInfraName() == null || infraDetails.getInfraName().isEmpty() ||
            infraDetails.getInstanName() == null || infraDetails.getInstanName().isEmpty()) {
            throw new IllegalArgumentException("Infrastructure Name was not defined in the request");
        }

        if (request.getStartDate().getTime() >= request.getEndDate().getTime()) {
            throw new IllegalArgumentException("Invalid Date Argument - FromDate should be less than ToDate");
        }

        infraDetails.setPriorityBasedAuthenticator();
    }

    public String getLogFilePath() {
        return currentLogsDir + File.separator + "extraction_" + request.getRequestID() + ".log";
    }

    public Level getLogLevel() {
        return (isDebugEnabled()) ? Level.DEBUG : Level.INFO;
    }

    public boolean isDebugEnabled() {
        if (request.getRequestConfig() != null && request.getRequestConfig().containsKey("debug")) {
            return Boolean.parseBoolean((String) request.getRequestConfig().get("debug"));
        }
        return ConnectorConstants.isDebugEnabled();
    }

    public Logger getLogger() {
        return logger;
    }

    public String getCurrentExtractionDataFile() {
        return currentExtractionDataFile;
    }

    public String getCurrentDataDir() {
        return currentDataDir;
    }

    public String getCurrentUpdateDir() {
        return currentUpdateDir;
    }

    public RequestDetails getRequest() {
        return request;
    }

    public InfrastructureDetails getInfraDetails() {
        return infraDetails;
    }
}