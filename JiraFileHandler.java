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
    public static final String UIX_DIR = "uix_invalid_csv_files";

    /**
     * Processes the CSV file and returns a File object.
     *
     * @param data The MultipartFile containing CSV data
     * @param userToken The UserToken for the current user
     * @return A File object containing the processed CSV data
     * @throws IOException If there's an error in file processing
     */
    public File processCSVFile(MultipartFile data, UserToken userToken) throws IOException {
        String csvContents = MultipartUtil.getData(data, null);
        File dir = createTempDir();
        return writeCSVToFile(csvContents, dir, userToken);
    }

    private File createTempDir() throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tmpDir, UIX_DIR);
        if (!dir.exists() && !dir.mkdir()) {
            throw new IOException("Failed to create directory: " + dir.getAbsolutePath());
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