package com.blueoptima.uix.processor;

import com.blueoptima.uix.csv.FileSeparator;
import com.blueoptima.uix.dto.Message;
import com.blueoptima.uix.security.UserToken;
import com.blueoptima.uix.service.JiraService;
import com.blueoptima.uix.util.MultipartUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Component
public class FileProcessor {

    private static final Logger logger = LoggerFactory.getLogger(FileProcessor.class);
    public static final String UIX_DIR = "uix_invalid_csv_files";

    @Autowired
    private JiraService jiraService;

    public Message processFileAndRaiseTicket(MultipartFile data, UserToken userToken) throws IOException {
        String csvContents = MultipartUtil.getData(data, null);
        File file = null;
        Message message;

        try {
            file = createTempFile(csvContents, userToken);
            message = jiraService.raiseTSUP(file);
        } catch (IOException e) {
            logger.error("Error in file reading: ", e);
            throw e;
        } finally {
            if (file != null && file.exists()) {
                file.delete();
            }
        }

        return message;
    }

    private File createTempFile(String csvContents, UserToken userToken) throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tmpDir, UIX_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }

        if (csvContents != null) {
            File file = new File(dir, FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userToken.getUserId());
            FileUtils.writeStringToFile(file, csvContents);
            return file;
        }

        return null;
    }
}