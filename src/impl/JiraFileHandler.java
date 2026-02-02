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

    public File handleFileUpload(MultipartFile data, String userId) throws IOException {
        String csvContents = MultipartUtil.getData(data, null);
        File dir = createTempDir();
        return writeCSVToFile(csvContents, dir, userId);
    }

    private File createTempDir() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tmpDir, UIX_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    private File writeCSVToFile(String csvContents, File dir, String userId) throws IOException {
        if (csvContents != null) {
            File file = new File(dir, FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userId);
            FileUtils.writeStringToFile(file, csvContents);
            return file;
        }
        return null;
    }
}