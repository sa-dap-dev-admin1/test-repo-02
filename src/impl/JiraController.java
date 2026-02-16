package com.blueoptima.uix.controller;
//test 2334ybfhdsdjfksf  Tesst Pub 3
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
        
        // Handle file creation and JIRA ticket raising
        return handleFileCreation(csvContents, userToken);
    }

    private Message handleFileCreation(String csvContents, UserToken userToken) throws IOException {
        if (csvContents == null || csvContents.isEmpty()) {
            logger.error("CSV contents are null or empty");
            throw new IllegalArgumentException("CSV contents cannot be null or empty");
        }

        File tempDir = createTemporaryDirectory();
        File file = writeCSVToFile(csvContents, tempDir, userToken);

        try {
            Message message = jiraService.raiseTSUP(file);
            if (message == null) {
                logger.warn("JiraService returned null message");
                return new Message("JIRA ticket creation failed");
            }
            return message;
        } catch (Exception e) {
            logger.error("Error raising JIRA ticket: ", e);
            throw new IOException("Failed to raise JIRA ticket", e);
        } finally {
            if (file != null && file.exists()) {
                file.delete();
            }
        }
    }

    private File createTemporaryDirectory() throws IOException {
        Path tempPath = Paths.get(System.getProperty("java.io.tmpdir"), UIX_DIR);
        return Files.createDirectories(tempPath).toFile();
    }

    private File writeCSVToFile(String csvContents, File dir, UserToken userToken) throws IOException {
        String fileName = generateFileName(userToken);
        File file = new File(dir, fileName);
        try {
            FileUtils.writeStringToFile(file, csvContents);
            return file;
        } catch (IOException e) {
            logger.error("Error writing CSV to file: ", e);
            throw e;
        }
    }

    private String generateFileName(UserToken userToken) {
        return FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userToken.getUserId();
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