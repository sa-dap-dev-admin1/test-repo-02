package com.blueoptima.uix.controller;

import com.blueoptima.iam.dto.PermissionsCode;
import com.blueoptima.uix.SkipValidationCheck;
import com.blueoptima.uix.annotations.CSVConverter;
import com.blueoptima.uix.csv.FileSeparator;
import com.blueoptima.uix.dto.Message;
import com.blueoptima.uix.security.UserToken;
import com.blueoptima.uix.security.auth.AccessCode;
import com.blueoptima.uix.service.JiraService;
import com.blueoptima.uix.util.MultipartUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class JiraController {

    @Autowired
    private JiraService jiraService;

    private static final Logger logger = LoggerFactory.getLogger(JiraController.class);

    public static final String UIX_DIR = "uix_invalid_csv_files";

    @RequestMapping(name = "Request to raise a ticket", value = "/v1/admin/jira/issue", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @AccessCode(PermissionsCode.DEVELOPER_READ + PermissionsCode.DEVELOPER_WRITE)
    @SkipValidationCheck
    @CSVConverter
    public Message raiseJiraTicket(@RequestBody MultipartFile data) throws IOException {
        UserToken userToken = (UserToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // Extract CSV contents from MultipartFile
        String csvContents = extractCsvContents(data);
        
        // Create temporary file
        File file = createTemporaryFile(csvContents, userToken);
        
        // Raise JIRA ticket
        return raiseJiraTicket(file);
    }

    private String extractCsvContents(MultipartFile data) throws IOException {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Invalid or empty file");
        }
        return MultipartUtil.getData(data, null);
    }

    private File createTemporaryFile(String csvContents, UserToken userToken) throws IOException {
        Path tempDir = createTempDirectory();
        String fileName = generateFileName(userToken);
        Path filePath = tempDir.resolve(fileName);
        
        Files.write(filePath, csvContents.getBytes());
        return filePath.toFile();
    }

    private Path createTempDirectory() throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        Path dir = Paths.get(tmpDir, UIX_DIR);
        Files.createDirectories(dir);
        return dir;
    }

    private String generateFileName(UserToken userToken) {
        return FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userToken.getUserId();
    }

    private Message raiseJiraTicket(File file) {
        try {
            return jiraService.raiseTSUP(file);
        } catch (IOException e) {
            logger.error("Error in file reading: ", e);
            throw new RuntimeException("Failed to raise JIRA ticket", e);
        } finally {
            if (file != null && file.exists()) {
                file.delete();
            }
        }
    }
}