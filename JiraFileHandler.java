package com.blueoptima.uix.util;

import com.blueoptima.uix.csv.FileSeparator;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Component
public class JiraFileHandler {

    private static final Logger logger = LoggerFactory.getLogger(JiraFileHandler.class);
    private static final String UIX_DIR = "uix_invalid_csv_files";

    public String extractCsvContents(MultipartFile data) throws IOException {
        return MultipartUtil.getData(data, null);
    }

    public File createTempFile(String csvContents, String userId) throws IOException {
        if (csvContents == null) {
            return null;
        }

        String tmpDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tmpDir, UIX_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }

        File file = new File(dir, FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userId);
        try {
            FileUtils.writeStringToFile(file, csvContents);
        } catch (IOException e) {
            logger.error("Error in file writing: ", e);
            throw e;
        }
        return file;
    }
}