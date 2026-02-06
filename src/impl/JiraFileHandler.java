package com.blueoptima.uix.controller;

import com.blueoptima.uix.csv.FileSeparator;
import com.blueoptima.uix.util.MultipartUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Component
public class JiraFileHandler {

    public static final String UIX_DIR = "uix_invalid_csv_files";

    public File handleFile(MultipartFile data, String userId) throws IOException {
        String csvContents = MultipartUtil.getData(data, null);
        return createFile(csvContents, userId);
    }

    private File createFile(String csvContents, String userId) throws IOException {
        File dir = createDirectory();
        if (csvContents != null) {
            String fileName = generateFileName(userId);
            File file = new File(dir, fileName);
            FileUtils.writeStringToFile(file, csvContents);
            return file;
        }
        return null;
    }

    private File createDirectory() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tmpDir, UIX_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    private String generateFileName(String userId) {
        return FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userId;
    }
}