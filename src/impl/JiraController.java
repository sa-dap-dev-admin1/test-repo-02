package com.blueoptima.uix.controller;
//test 2334ybfhdsd
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
import java.util.Optional;

@RestController
public class JiraController {

    @Autowired
    private JiraService jiraService;

    private static final Logger logger = LoggerFactory.getLogger(JiraController.class);

    public static final String UIX_DIR = "uix_invalid_csv_files";
    private static final String FILE_PREFIX = FileSeparator.CSV_SEPARATOR.getName() + "_";

    @RequestMapping(name = "Request to raise a ticket", value = "/v1/admin/jira/issue", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @AccessCode(PermissionsCode.DEVELOPER_READ + PermissionsCode.DEVELOPER_WRITE)
    @SkipValidationCheck
    @CSVConverter
    public Message raiseJiraTicket(@RequestBody MultipartFile data) throws IOException {
        logger.info("Starting to process Jira ticket request");
        UserToken userToken = (UserToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String csvContents = MultipartUtil.getData(data, null);

        if (csvContents == null || csvContents.isEmpty()) {
            logger.error("CSV contents are empty or null");
            throw new IllegalArgumentException("CSV contents cannot be empty or null");
        }

        Optional<File> file = createTemporaryFile(csvContents, userToken.getUserId());
        
        if (!file.isPresent()) {
            logger.error("Failed to create temporary file");
            throw new IOException("Failed to create temporary file");
        }

        try {
            Message message = jiraService.raiseTSUP(file.get());
            logger.info("Successfully processed Jira ticket request");
            return message;
        } catch (IOException e) {
            logger.error("Error in processing Jira ticket: ", e);
            throw e;
        } finally {
            if (file.isPresent()) {
                boolean deleted = file.get().delete();
                if (!deleted) {
                    logger.warn("Failed to delete temporary file: {}", file.get().getAbsolutePath());
                }
            }
        }
    }

    private Optional<File> createTemporaryFile(String csvContents, String userId) {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tmpDir, UIX_DIR);
        
        if (!dir.exists() && !dir.mkdir()) {
            logger.error("Failed to create directory: {}", dir.getAbsolutePath());
            return Optional.empty();
        }

        File file = new File(dir, FILE_PREFIX + System.currentTimeMillis() + "X" + userId);
        
        try {
            FileUtils.writeStringToFile(file, csvContents);
            logger.info("Temporary file created: {}", file.getAbsolutePath());
            return Optional.of(file);
        } catch (IOException e) {
            logger.error("Error writing to file: ", e);
            return Optional.empty();
        }
    }

    public int maxSubArray(int[] nums) {
        int currentSum = nums[0]; // Start with the first element
        int maxSum = nums[0];     // Initialize maxSum with the first element

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