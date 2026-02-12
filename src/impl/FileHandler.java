package com.blueoptima.uix.controller;

import com.blueoptima.uix.csv.FileSeparator;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class FileHandler {

    private static final Logger logger = LoggerFactory.getLogger(FileHandler.class);
    public static final String UIX_DIR = "uix_invalid_csv_files";

    public File createFile(String csvContents, String userId) throws IOException {
        File dir = createDirectory();
        if (csvContents == null) {
            logger.warn("CSV contents is null");
            return null;
        }
        File file = new File(dir, generateFileName(userId));
        writeToFile(file, csvContents);
        return file;
    }

    private File createDirectory() throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tmpDir, UIX_DIR);
        if (!dir.exists() && !dir.mkdir()) {
            throw new IOException("Failed to create directory: " + dir.getAbsolutePath());
        }
        return dir;
    }

    private String generateFileName(String userId) {
        return FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userId;
    }

    private void writeToFile(File file, String content) throws IOException {
        try {
            FileUtils.writeStringToFile(file, content);
        } catch (IOException e) {
            logger.error("Error writing to file: " + file.getAbsolutePath(), e);
            throw e;
        }
    }
}