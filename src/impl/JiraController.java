package com.blueoptima.uix.controller;
//test 2334ybfhdsdjfksf  Test Pub 3
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
        String csvContents = extractCsvContents(data);
        
        try {
            File tempDir = createTempDirectory();
            Optional<File> fileOptional = writeFileToDirectory(tempDir, csvContents, userToken.getUserId());
            return handleJiraTicketCreation(fileOptional);
        } catch (IOException e) {
            logger.error("Error in file processing: ", e);
            throw e;
        }
    }

    private String extractCsvContents(MultipartFile data) throws IOException {
        logger.debug("Extracting CSV contents from MultipartFile");
        return MultipartUtil.getData(data, null);
    }

    private File createTempDirectory() throws IOException {
        logger.debug("Creating temporary directory");
        String tmpDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tmpDir, UIX_DIR);
        if (!dir.exists() && !dir.mkdir()) {
            throw new IOException("Failed to create directory: " + dir.getAbsolutePath());
        }
        return dir;
    }

    private Optional<File> writeFileToDirectory(File dir, String csvContents, String userId) throws IOException {
        if (csvContents == null) {
            logger.warn("CSV contents are null, skipping file creation");
            return Optional.empty();
        }
        
        logger.debug("Writing CSV contents to file");
        String fileName = FILE_PREFIX + System.currentTimeMillis() + "X" + userId;
        File file = new File(dir, fileName);
        FileUtils.writeStringToFile(file, csvContents);
        return Optional.of(file);
    }

    private Message handleJiraTicketCreation(Optional<File> fileOptional) throws IOException {
        logger.info("Handling Jira ticket creation");
        if (!fileOptional.isPresent()) {
            logger.warn("No file present for Jira ticket creation");
            return new Message("No file provided for Jira ticket creation");
        }
        return jiraService.raiseTSUP(fileOptional.get());
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