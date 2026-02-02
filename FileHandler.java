package com.blueoptima.uix.util;

import com.blueoptima.uix.csv.FileSeparator;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Component
public class FileHandler {

    private static final Logger logger = LoggerFactory.getLogger(FileHandler.class);
    public static final String UIX_DIR = "uix_invalid_csv_files";

    public String getFileContents(MultipartFile data) throws IOException {
        return MultipartUtil.getData(data, null);
    }

    public File createTempFile(String userId, String csvContents) throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tmpDir, UIX_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }

        File file = new File(dir, FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userId);
        try {
            FileUtils.writeStringToFile(file, csvContents);
        } catch (IOException e) {
            logger.error("Error in file writing: ", e);
            throw e;
        }
        return file;
    }
}