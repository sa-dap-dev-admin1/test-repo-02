package com.blueoptima.uix.service;

import com.blueoptima.uix.csv.FileSeparator;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class FileHandlingService {

    public static final String UIX_DIR = "uix_invalid_csv_files";

    public File createTemporaryFile(String csvContents, String userId) throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tmpDir, UIX_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }

        if (csvContents != null) {
            File file = new File(dir, FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userId);
            FileUtils.writeStringToFile(file, csvContents);
            return file;
        }
        return null;
    }

    public void cleanupTemporaryFile(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
}