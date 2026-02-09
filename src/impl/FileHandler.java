package com.blueoptima.uix.util;

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
public class FileHandler {

    private static final Logger logger = LoggerFactory.getLogger(FileHandler.class);
    public static final String UIX_DIR = "uix_invalid_csv_files";

    public File createFileFromMultipartData(MultipartFile data, UserToken userToken) throws IOException {
        String csvContents = MultipartUtil.getData(data, null);
        File file = null;

        try {
            File dir = createTempDirectory();
            if (csvContents != null) {
                file = createFile(dir, userToken);
                FileUtils.writeStringToFile(file, csvContents);
            }
        } catch (IOException e) {
            logger.error("Error in file handling: ", e);
            throw e;
        }

        return file;
    }

    private File createTempDirectory() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tmpDir, UIX_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    private File createFile(File dir, UserToken userToken) {
        return new File(dir, FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userToken.getUserId());
    }
}