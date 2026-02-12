package com.blueoptima.uix.util;

import com.blueoptima.uix.csv.FileSeparator;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class FileHandlerUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileHandlerUtil.class);

    public static final String UIX_DIR = "uix_invalid_csv_files";

    public static File createOrGetDirectory(String dirName) {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tmpDir, dirName);
        if (!dir.exists()) {
            boolean created = dir.mkdir();
            if (!created) {
                logger.warn("Failed to create directory: {}", dir.getAbsolutePath());
            }
        }
        return dir;
    }

    public static String generateFileName(String userId) {
        return FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userId;
    }

    public static File writeContentToFile(File dir, String fileName, String content) throws IOException {
        File file = new File(dir, fileName);
        try {
            FileUtils.writeStringToFile(file, content);
            return file;
        } catch (IOException e) {
            logger.error("Error writing to file: {}", file.getAbsolutePath(), e);
            throw e;
        }
    }
}