package com.blueoptima.uix.controller;

import com.blueoptima.uix.csv.FileSeparator;
import com.blueoptima.uix.security.UserToken;
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

    public File createFileFromCsv(UserToken userToken, String csvContents) throws IOException {
        if (csvContents == null) {
            return null;
        }

        File dir = createDirectoryIfNotExists();
        File file = createFile(dir, userToken);
        FileUtils.writeStringToFile(file, csvContents);
        return file;
    }

    private File createDirectoryIfNotExists() {
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