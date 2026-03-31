package com.blueoptima.uix.controller;

import com.blueoptima.uix.csv.FileSeparator;
import com.blueoptima.uix.security.UserToken;
import com.blueoptima.uix.util.MultipartUtil;
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

    public File processFile(MultipartFile data, UserToken userToken) throws IOException {
        String csvContents = MultipartUtil.getData(data, null);
        File tempDir = createTempDir();
        return writeFile(tempDir, csvContents, userToken);
    }

    private File createTempDir() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tmpDir, UIX_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    private File writeFile(File dir, String csvContents, UserToken userToken) throws IOException {
        if (csvContents == null) {
            logger.warn("CSV contents is null");
            return null;
        }

        String fileName = generateFileName(userToken);
        File file = new File(dir, fileName);
        
        try {
            FileUtils.writeStringToFile(file, csvContents);
            return file;
        } catch (IOException e) {
            logger.error("Error writing file: ", e);
            throw e;
        }
    }

    private String generateFileName(UserToken userToken) {
        return FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userToken.getUserId();
    }
}