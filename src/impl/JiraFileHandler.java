package com.blueoptima.uix.controller;

import com.blueoptima.uix.csv.FileSeparator;
import com.blueoptima.uix.security.UserToken;
import com.blueoptima.uix.util.MultipartUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Component
public class JiraFileHandler {

    private static final Logger logger = LoggerFactory.getLogger(JiraFileHandler.class);

    @Value("${uix.invalid.csv.dir}")
    private String uixDir;

    public String extractCSVContents(MultipartFile data) throws IOException {
        return MultipartUtil.getData(data, null);
    }

    public File createFile(String csvContents, UserToken userToken) throws IOException {
        if (csvContents == null) {
            throw new IllegalArgumentException("CSV contents cannot be null");
        }

        String tmpDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tmpDir, uixDir);
        if (!dir.exists() && !dir.mkdir()) {
            throw new IOException("Failed to create directory: " + dir.getAbsolutePath());
        }

        File file = new File(dir, generateFileName(userToken));
        try {
            FileUtils.writeStringToFile(file, csvContents);
            logger.info("CSV file created: {}", file.getAbsolutePath());
            return file;
        } catch (IOException e) {
            logger.error("Error in file writing: ", e);
            throw new FileCreationException("Failed to create CSV file", e);
        }
    }

    private String generateFileName(UserToken userToken) {
        return FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userToken.getUserId();
    }
}