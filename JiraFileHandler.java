package com.blueoptima.uix.controller;

import com.blueoptima.uix.csv.FileSeparator;
import com.blueoptima.uix.security.UserToken;
import com.blueoptima.uix.util.MultipartUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Component
public class JiraFileHandler {

    public static final String UIX_DIR = "uix_invalid_csv_files";

    public String extractCsvContents(MultipartFile data) throws IOException {
        return MultipartUtil.getData(data, null);
    }

    public File createTempFile(String csvContents, UserToken userToken) throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tmpDir, UIX_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }

        if (csvContents != null) {
            File file = new File(dir, generateFileName(userToken));
            FileUtils.writeStringToFile(file, csvContents);
            return file;
        }
        return null;
    }

    public void cleanupTempFile(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    private String generateFileName(UserToken userToken) {
        return FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userToken.getUserId();
    }
}