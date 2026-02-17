package com.blueoptima.connectors.scr.managers;

import com.blueoptima.connectors.BOpExtractionData;
import com.blueoptima.connectors.common.message.RequestDetails;
import com.blueoptima.connectors.common.error.BOpException;
import com.blueoptima.connectors.scr.common.BOpSCRData;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

public class DataPersistenceManager {
    private BOpSCRData scrData;
    private String dataFilePath;

    public void saveData(RequestDetails request) throws BOpException {
        if (scrData == null) {
            throw new BOpException("No data to save");
        }

        dataFilePath = request.getDataDir() + File.separator + "extraction_" + request.getRequestID() + ".data";
        
        try (FileOutputStream fos = new FileOutputStream(dataFilePath);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(scrData);
        } catch (IOException e) {
            throw new BOpException("Failed to save extraction data", e);
        }
    }

    public BOpExtractionData getExtractionData() throws BOpException {
        if (dataFilePath == null) {
            throw new BOpException("No data file path set");
        }

        BOpExtractionData extractionData = new BOpExtractionData();
        extractionData.setFileName(dataFilePath);
        extractionData.setRequestID(scrData.getRequestID());
        return extractionData;
    }

    public void setScrData(BOpSCRData scrData) {
        this.scrData = scrData;
    }
}