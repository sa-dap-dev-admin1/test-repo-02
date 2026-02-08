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
        String csvContents = MultipartUtil.getData(data, null);

        if (csvContents == null || csvContents.isEmpty()) {
            throw new IllegalArgumentException("CSV contents cannot be null or empty");
        }

        File file = createCsvFile(csvContents, userToken.getUserId());
        return jiraService.raiseTSUP(file);
    }

    private File createCsvFile(String csvContents, String userId) throws IOException {
        Path dir = createDirectoryIfNotExists();
        String fileName = generateFileName(userId);
        Path filePath = dir.resolve(fileName);

        try {
            return Files.write(filePath, csvContents.getBytes()).toFile();
        } catch (IOException e) {
            logger.error("Error in file writing: ", e);
            throw new IOException("Failed to write CSV file", e);
        }
    }

    private Path createDirectoryIfNotExists() throws IOException {
        Path dir = Paths.get(System.getProperty("java.io.tmpdir"), UIX_DIR);
        if (!Files.exists(dir)) {
            try {
                Files.createDirectory(dir);
            } catch (IOException e) {
                logger.error("Error creating directory: ", e);
                throw new IOException("Failed to create directory", e);
            }
        }
        return dir;
    }

    private String generateFileName(String userId) {
        return FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userId;
    }

    // Testing UAT : 2
}