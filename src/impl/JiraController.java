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
import java.util.Optional;

@RestController
public class JiraController {

    @Autowired
    private JiraService jiraService;

    private static final Logger logger = LoggerFactory.getLogger(JiraController.class);

    public static final String UIX_DIR = "uix_invalid_csv_files";
    private static final String FILE_NAME_PATTERN = "%s_%dX%d";
    private static final String ERROR_FILE_READING = "Error in file reading: ";

    @RequestMapping(name = "Request to raise a ticket", value = "/v1/admin/jira/issue", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @AccessCode(PermissionsCode.DEVELOPER_READ + PermissionsCode.DEVELOPER_WRITE)
    @SkipValidationCheck
    @CSVConverter
    public Message raiseJiraTicket(@RequestBody MultipartFile data) throws IOException {
        logger.info("Starting to raise Jira ticket");
        UserToken userToken = (UserToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        Optional<String> csvContents = extractCsvContents(data);
        if (!csvContents.isPresent()) {
            logger.warn("CSV contents are empty");
            return new Message("CSV contents are empty");
        }

        File tempDir = createTempDirectory();
        File file = writeToTempFile(tempDir, csvContents.get(), userToken.getUserId());
        
        return callJiraService(file);
    }

    private Optional<String> extractCsvContents(MultipartFile data) {
        logger.info("Extracting CSV contents");
        return Optional.ofNullable(MultipartUtil.getData(data, null));
    }

    private File createTempDirectory() throws IOException {
        logger.info("Creating temporary directory");
        String tmpDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tmpDir, UIX_DIR);
        if (!dir.exists() && !dir.mkdir()) {
            throw new IOException("Failed to create directory: " + dir.getAbsolutePath());
        }
        return dir;
    }

    private File writeToTempFile(File dir, String csvContents, long userId) throws IOException {
        logger.info("Writing CSV contents to temporary file");
        String fileName = String.format(FILE_NAME_PATTERN, FileSeparator.CSV_SEPARATOR.getName(), System.currentTimeMillis(), userId);
        File file = new File(dir, fileName);
        try {
            FileUtils.writeStringToFile(file, csvContents);
        } catch (IOException e) {
            logger.error(ERROR_FILE_READING, e);
            throw e;
        }
        return file;
    }

    private Message callJiraService(File file) throws IOException {
        logger.info("Calling Jira service to raise TSUP");
        try {
            return jiraService.raiseTSUP(file);
        } catch (IOException e) {
            logger.error(ERROR_FILE_READING, e);
            throw e;
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