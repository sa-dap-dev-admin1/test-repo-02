package com.blueoptima.uix.util;

import com.blueoptima.uix.csv.FileSeparator;
import com.blueoptima.uix.security.UserToken;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Component
public class FileHandler {

    private static final String UIX_DIR = "uix_invalid_csv_files";

    public File handleFileUpload(MultipartFile data, UserToken userToken) throws IOException {
        String csvContents = MultipartUtil.getData(data, null);
        File dir = createTempDirectory();
        return createFile(dir, csvContents, userToken);
    }

    private File createTempDirectory() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tmpDir, UIX_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    private File createFile(File dir, String csvContents, UserToken userToken) throws IOException {
        if (csvContents == null) {
            return null;
        }
        File file = new File(dir, generateFileName(userToken));
        FileUtils.writeStringToFile(file, csvContents);
        return file;
    }

    private String generateFileName(UserToken userToken) {
        return FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userToken.getUserId();
    }
}