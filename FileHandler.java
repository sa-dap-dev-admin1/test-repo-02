package com.blueoptima.uix.util;

import com.blueoptima.uix.csv.FileSeparator;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class FileHandler {

    public static final String UIX_DIR = "uix_invalid_csv_files";

    public File createFile(String csvContents, String userId) throws IOException {
        File dir = createTempDirectory();
        if (csvContents != null) {
            File file = new File(dir, generateFileName(userId));
            FileUtils.writeStringToFile(file, csvContents);
            return file;
        }
        return null;
    }

    private File createTempDirectory() {
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