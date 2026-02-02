package com.blueoptima.uix.util;

import com.blueoptima.uix.csv.FileSeparator;
import com.blueoptima.uix.security.UserToken;
import com.blueoptima.uix.util.MultipartUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Component
public class JiraFileProcessor {

    public static final String UIX_DIR = "uix_invalid_csv_files";

    public String extractFileContents(MultipartFile data) throws IOException {
        return MultipartUtil.getData(data, null);
    }

    public File createFile(String csvContents, UserToken userToken) throws IOException {
        File dir = createTempDirectory();
        if (csvContents != null) {
            return writeFile(dir, csvContents, userToken);
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

    private File writeFile(File dir, String csvContents, UserToken userToken) throws IOException {
        File file = new File(dir, generateFileName(userToken));
        FileUtils.writeStringToFile(file, csvContents);
        return file;
    }

    private String generateFileName(UserToken userToken) {
        return FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userToken.getUserId();
    }
}