package com.blueoptima.connectors.scr;

import com.blueoptima.connectors.common.message.RequestDetails;
import com.blueoptima.connectors.common.error.BOpException;
import com.blueoptima.connectors.ConnectorConstants;
import com.blueoptima.connectors.main.VersionReader;
import org.apache.logging.log4j.Level;

import java.io.File;

public class SCRConfiguration {
    private final RequestDetails request;
    private String logsDir;
    private String dataDir;
    private String updateDir;
    private String extractionDataFile;

    public SCRConfiguration(RequestDetails request) {
        this.request = request;
    }

    public void initDirectories(String dir) throws IllegalAccessException {
        logsDir = ConnectorConstants.EXTRACTION_LOGS_DIR;
        dataDir = ConnectorConstants.EXTRACTION_DATA_DIR + File.separator + dir + File.separator + request.getRequestID();
        updateDir = dataDir + File.separator + ConnectorConstants.DEF_UPDATE_DIR;
        extractionDataFile = dataDir + File.separator + "Data_" + request.getRequestID() + ConnectorConstants.FILE_EXTENSION_SEARIALIZED;

        new File(logsDir).mkdirs();
        new File(dataDir).mkdirs();
        new File(updateDir).mkdirs();
    }

    public boolean validateRequest() throws BOpException {
        if (request.getInfraDetails().getInstanUrl() == null || request.getInfraDetails().getInstanUrl().isEmpty()) {
            throw new BOpException(ExtractionStatus.VALIDATE_DETAIL, GenericErrorCode.RCE0001, "URL was not defined in the request");
        }

        if (request.getInfraDetails().getInfraName() == null || request.getInfraDetails().getInfraName().isEmpty() ||
            request.getInfraDetails().getInstanName() == null || request.getInfraDetails().getInstanName().isEmpty()) {
            throw new BOpException(ExtractionStatus.VALIDATE_DETAIL, GenericErrorCode.RCE0001, "Infrastructure Name was not defined in the request");
        }

        if (request.getStartDate().getTime() >= request.getEndDate().getTime()) {
            throw new BOpException(ExtractionStatus.VALIDATE_DETAIL, GenericErrorCode.RCE0001, "Invalid Date Argument - FromDate should be less than ToDate");
        }

        return true;
    }

    public String getLogFilePath() {
        return logsDir + File.separator + "extraction_" + request.getRequestID() + ".log";
    }

    public Level getLogLevel() {
        Boolean debugOn = false;
        if (request.getRequestConfig() != null && request.getRequestConfig().containsKey("debug")) {
            debugOn = Boolean.parseBoolean((String) request.getRequestConfig().get("debug"));
        }
        return (ConnectorConstants.isDebugEnabled() || debugOn) ? Level.DEBUG : Level.INFO;
    }

    public String getIntegratorVersion() {
        return VersionReader.getInstance().getVersion();
    }

    public String getLogsDir() {
        return logsDir;
    }

    public String getUpdateDir() {
        return updateDir;
    }

    public String getExtractionDataFile() {
        return extractionDataFile;
    }

    public RequestDetails getRequest() {
        return request;
    }
}