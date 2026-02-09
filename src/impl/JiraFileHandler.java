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

    public String extractCsvContents(MultipartFile data) throws IOException {
        return MultipartUtil.getData(data, null);
    }

    public String createFileWithContents(String csvContents, String userId) throws IOException {
        File dir = createDirectoryIfNotExists();
        File file = generateUniqueFile(dir, userId);
        FileUtils.writeStringToFile(file, csvContents);
        return file.getAbsolutePath();
    }

    private File createDirectoryIfNotExists() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tmpDir, UIX_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    private File generateUniqueFile(File dir, String userId) {
        String fileName = FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userId;
        return new File(dir, fileName);
    }
}