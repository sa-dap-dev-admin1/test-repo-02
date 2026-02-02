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

    /**
     * Raises a Jira ticket based on the provided CSV data.
     *
     * @param csvData The MultipartFile containing CSV data
     * @return A Message object with the result of the operation
     * @throws IOException If there's an error processing the file
     */
    @RequestMapping(name = "Request to raise a ticket", value = "/v1/admin/jira/issue", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @AccessCode(PermissionsCode.DEVELOPER_READ + PermissionsCode.DEVELOPER_WRITE)
    @SkipValidationCheck
    @CSVConverter
    public Message raiseJiraTicket(@RequestBody MultipartFile csvData) throws IOException {
        if (csvData == null || csvData.isEmpty()) {
            throw new IllegalArgumentException("CSV data must not be null or empty");
        }

        UserToken userToken = (UserToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String csvContents = MultipartUtil.getData(csvData, null);
        
        File csvFile = processCsvFile(csvContents, userToken.getUserId());
        
        try {
            return jiraService.raiseTSUP(csvFile);
        } catch (IOException e) {
            logger.error("Error in raising Jira ticket: ", e);
            throw e;
        } finally {
            if (csvFile != null && csvFile.exists()) {
                csvFile.delete();
            }
        }
    }

    private File processCsvFile(String csvContents, String userId) throws IOException {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), UIX_DIR);
        Files.createDirectories(tempDir);

        String fileName = String.format("%s_%d_X%s", FileSeparator.CSV_SEPARATOR.getName(), System.currentTimeMillis(), userId);
        Path filePath = tempDir.resolve(fileName);

        try {
            Files.writeString(filePath, csvContents);
            return filePath.toFile();
        } catch (IOException e) {
            logger.error("Error in writing CSV file: ", e);
            throw e;
        }
    }

    public int maxSubArray(int[] nums) {
        int currentSum = nums[0];
        int maxSum = nums[0];

        for (int i = 1; i < nums.length; i++) {
            currentSum = Math.max(nums[i], currentSum + nums[i]);
            maxSum = Math.max(maxSum, currentSum);
        }
        return maxSum;
    }
}