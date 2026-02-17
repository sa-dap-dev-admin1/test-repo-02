package com.blueoptima.connectors.scr.managers;

import com.blueoptima.connectors.common.message.RequestDetails;
import com.blueoptima.connectors.common.error.BOpException;
import java.util.Date;

public class ConfigurationManager {
    private String dataDir;

    public ConfigurationManager(RequestDetails request) {
        // Initialize with request details if needed
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public boolean validateRequest(RequestDetails request) throws BOpException {
        if (request == null) {
            throw new BOpException("Request details are null");
        }

        if (request.getInfraDetails() == null) {
            throw new BOpException("Infrastructure details are null");
        }

        if (request.getInfraDetails().getInstanUrl() == null || request.getInfraDetails().getInstanUrl().isEmpty()) {
            throw new BOpException("Repository URL is not defined");
        }

        if (request.getInfraDetails().getInfraName() == null || request.getInfraDetails().getInfraName().isEmpty()) {
            throw new BOpException("Infrastructure name is not defined");
        }

        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new BOpException("Start date or end date is null");
        }

        if (request.getStartDate().after(request.getEndDate())) {
            throw new BOpException("Start date is after end date");
        }

        return true;
    }

    public String getDataDir() {
        return dataDir;
    }
}