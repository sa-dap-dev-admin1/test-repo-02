package com.blueoptima.uix.controller;

import com.blueoptima.uix.csv.FileSeparator;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class JiraFileHandler {

    private static final Logger logger = LoggerFactory.getLogger(JiraFileHandler.class);
    public static final String UIX_DIR = "uix_invalid_csv_files";

    public File createFile(String csvContents, String userId) throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tmpDir, UIX_DIR);
        createDirectoryIfNotExists(dir);

        File file = new File(dir, generateFileName(userId));
        writeContentToFile(file, csvContents);

        return file;
    }

    private void createDirectoryIfNotExists(File dir) {
        if (!dir.exists()) {
            boolean created = dir.mkdir();
            if (!created) {
                logger.warn("Failed to create directory: {}", dir.getAbsolutePath());
            }
        }
    }

    private String generateFileName(String userId) {
        return FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userId;
    }

    private void writeContentToFile(File file, String content) throws IOException {
        try {
            FileUtils.writeStringToFile(file, content);
        } catch (IOException e) {
            logger.error("Error writing to file: {}", file.getAbsolutePath(), e);
            throw e;
        }
    }
}