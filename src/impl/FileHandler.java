package com.blueoptima.uix.util;

import com.blueoptima.uix.csv.FileSeparator;
import com.blueoptima.uix.security.UserToken;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class FileHandler {

    public static final String UIX_DIR = "uix_invalid_csv_files";

    public File createTempDirectory() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tmpDir, UIX_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    public File writeStringToFile(File dir, String content, UserToken userToken) throws IOException {
        if (content == null) {
            return null;
        }
        File file = new File(dir, FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userToken.getUserId());
        FileUtils.writeStringToFile(file, content);
        return file;
    }
}