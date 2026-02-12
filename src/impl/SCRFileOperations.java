package com.blueoptima.connectors.scr;

import com.blueoptima.connectors.common.BOpFileUtils;
import com.blueoptima.connectors.common.error.BOpException;
import com.blueoptima.connectors.scr.common.BOpSCRData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class SCRFileOperations {
    private static final Logger logger = LogManager.getLogger(SCRFileOperations.class);
    private final SCRConfiguration configuration;

    public SCRFileOperations(SCRConfiguration configuration) {
        this.configuration = configuration;
    }

    public void saveDataToFile(BOpSCRData scrData) throws BOpException {
        try {
            logger.info("Saving extraction data to file");
            BOpFileUtils.writeObjectToFile(configuration.getExtractionDataFile(), scrData);
            logger.info("Data saved successfully to {}", configuration.getExtractionDataFile());
        } catch (IOException e) {
            logger.error("Error saving data to file", e);
            throw new BOpException(ExtractionStatus.SAVE_DATA_TO_FILE, GenericErrorCode.RCE0001, "Failed to save data to file", e);
        }
    }

    public void writeExtractionDataAsJson(BOpSCRData scrData) throws BOpException {
        try {
            logger.info("Writing extraction data as JSON");
            String jsonFileName = configuration.getExtractionDataFile().replace(".ser", ".json");
            BOpFileUtils.writeJsonToFile(jsonFileName, scrData);
            logger.info("JSON data saved successfully to {}", jsonFileName);
        } catch (IOException e) {
            logger.error("Error writing JSON data to file", e);
            throw new BOpException(ExtractionStatus.SAVE_DATA_TO_FILE, GenericErrorCode.RCE0001, "Failed to write JSON data to file", e);
        }
    }
}