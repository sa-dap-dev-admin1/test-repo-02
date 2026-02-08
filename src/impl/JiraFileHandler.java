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

    public String getDataFromMultipartFile(MultipartFile data) throws IOException {
        return MultipartUtil.getData(data, null);
    }

    public File createFileFromCSV(String csvContents, UserToken userToken) throws IOException {
        File dir = createTempDirectory();
        return writeCSVToFile(csvContents, dir, userToken);
    }

    private File createTempDirectory() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tmpDir, UIX_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    private File writeCSVToFile(String csvContents, File dir, UserToken userToken) throws IOException {
        if (csvContents == null) {
            throw new IOException("CSV contents are null");
        }
        File file = new File(dir, generateFileName(userToken));
        try {
            FileUtils.writeStringToFile(file, csvContents);
        } catch (IOException e) {
            logger.error("Error writing CSV to file: ", e);
            throw e;
        }
        return file;
    }

    private String generateFileName(UserToken userToken) {
        return FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userToken.getUserId();
    }
}