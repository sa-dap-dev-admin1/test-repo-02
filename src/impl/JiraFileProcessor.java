package com.blueoptima.uix.service;

import com.blueoptima.uix.csv.FileSeparator;
import com.blueoptima.uix.util.MultipartUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class JiraFileProcessor {

    public static final String UIX_DIR = "uix_invalid_csv_files";

    public String extractCsvContents(MultipartFile data) throws IOException {
        return MultipartUtil.getData(data, null);
    }

    public File createFile(String csvContents, String userId) throws IOException {
        File dir = createDirectory();
        if (csvContents != null) {
            return writeToFile(dir, csvContents, userId);
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

    private File writeToFile(File dir, String csvContents, String userId) throws IOException {
        File file = new File(dir, buildFileName(userId));
        FileUtils.writeStringToFile(file, csvContents);
        return file;
    }

    private String buildFileName(String userId) {
        return FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userId;
    }
}