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
    private static final String FILE_NAME_PATTERN = "%s_%dX%d";
    private static final String ERROR_FILE_CREATION = "Error in file creation: ";

    @RequestMapping(name = "Request to raise a ticket", value = "/v1/admin/jira/issue", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @AccessCode(PermissionsCode.DEVELOPER_READ + PermissionsCode.DEVELOPER_WRITE)
    @SkipValidationCheck
    @CSVConverter
    public Message raiseJiraTicket(@RequestBody MultipartFile data) throws IOException {
        UserToken userToken = (UserToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String csvContents = MultipartUtil.getData(data, null);

        // Create temporary directory and file
        Path tempFile = handleFileCreation(csvContents, userToken.getUserId());

        // Raise TSUP ticket using JiraService
        return jiraService.raiseTSUP(tempFile.toFile());
    }

    private Path handleFileCreation(String csvContents, long userId) throws IOException {
        Path tempDir = createTempDirectory();
        return createFileFromCsvContents(tempDir, csvContents, userId);
    }

    private Path createTempDirectory() throws IOException {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), UIX_DIR);
        Files.createDirectories(tempDir);
        return tempDir;
    }

    private Path createFileFromCsvContents(Path dir, String csvContents, long userId) throws IOException {
        if (csvContents == null) {
            throw new IOException("CSV contents cannot be null");
        }

        String fileName = String.format(FILE_NAME_PATTERN, 
                FileSeparator.CSV_SEPARATOR.getName(), 
                System.currentTimeMillis(), 
                userId);
        Path file = dir.resolve(fileName);

        try {
            Files.write(file, csvContents.getBytes());
        } catch (IOException e) {
            logger.error(ERROR_FILE_CREATION, e);
            throw e;
        }

        return file;
    }

    public int maxSubArray(int[] nums) {
        int currentSum = nums[0]; // Start with the first element
        int maxSum = nums[0];     // Initialize maxSum with the first element
        //comment added
        // Traverse the array from the second element
        for (int i = 1; i < nums.length; i++) {
            // If currentSum is negative, reset to current element
            currentSum = Math.max(nums[i], currentSum + nums[i]);
            // Update maxSum if currentSum is greater
            maxSum = Math.max(maxSum, currentSum);
        }
        return maxSum;
    }
}