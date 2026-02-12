package com.blueoptima.connectors.scr.processors;

import com.blueoptima.connectors.common.message.BOpExtractionData;
import com.blueoptima.connectors.scr.managers.ConfigurationManager;
import com.blueoptima.connectors.common.BOpFileUtils;
import com.blueoptima.connectors.scr.common.BOpSCRData;
import com.google.gson.Gson;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;

public class ExtractionDataWriter {
    private final ConfigurationManager configManager;
    private final Logger logger;
    private String currentExtractionDataFile;
    private final Gson gson;

    public ExtractionDataWriter(ConfigurationManager configManager) {
        this.configManager = configManager;
        this.logger = configManager.getLogger();
        this.gson = new Gson();
    }

    public void setCurrentExtractionDataFile(String file) {
        this.currentExtractionDataFile = file;
    }

    public void saveDataToFile(BOpSCRData scrData) throws IOException {
        BOpFileUtils.writeObjectToFile(currentExtractionDataFile, scrData);
        writeExtractionDataAsJson(scrData);
    }

    private void writeExtractionDataAsJson(BOpSCRData scrData) throws IOException {
        String jsonFileName = currentExtractionDataFile.replace(".ser", ".json");
        try (FileWriter writer = new FileWriter(jsonFileName)) {
            gson.toJson(scrData, writer);
        }
    }

    public BOpExtractionData createExtractionData(BOpSCRData scrData) {
        BOpExtractionData bOpExtractionData = new BOpExtractionData();
        bOpExtractionData.setRequestID(configManager.getRequest().getRequestID());
        bOpExtractionData.setFileName(currentExtractionDataFile);
        
        if (configManager.getRequest().getRequestType().equals(com.blueoptima.cs.common.dto.message.RequestType.EXTRACTION)) {
            bOpExtractionData.computeIfEmpty(scrData);
        }
        
        return bOpExtractionData;
    }
}