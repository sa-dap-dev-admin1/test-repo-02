package com.blueoptima.uix.controller;
//test 2334ybfhdsdjfkf  Test Pub 2
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
import java.util.Optional;

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
        File file = null;
        Message message;

        //convert a multipart file to File. Test 10
        try {
            File tempDir = createTempDirectory();
            Optional<File> csvFile = writeCSVToFile(csvContents, tempDir, userToken.getUserId());
            message = handleJiraTicketCreation(csvFile);
        } catch (IOException e) {
            logger.error("Error in file processing: ", e);
            throw e;
        }

        return message;
    }

    private File createTempDirectory() throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        Path dirPath = Paths.get(tmpDir, UIX_DIR);
        // empty check here.
        return Files.createDirectories(dirPath).toFile();
    }

    private Optional<File> writeCSVToFile(String csvContents, File dir, String userId) throws IOException {
        // doing null check 
        if (csvContents == null) {
            return Optional.empty();
        }
        String fileName = FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userId;
        File file = new File(dir, fileName);
        FileUtils.writeStringToFile(file, csvContents);
        return Optional.of(file);
    }

    private Message handleJiraTicketCreation(Optional<File> csvFile) throws IOException {
        return csvFile.map(file -> {
            try {
                return jiraService.raiseTSUP(file);
            } catch (IOException e) {
                logger.error("Error raising JIRA ticket: ", e);
                throw new RuntimeException("Failed to raise JIRA ticket", e);
            }
        }).orElseThrow(() -> new IOException("CSV file could not be created"));
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