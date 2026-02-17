package com.blueoptima.uix.controller;
//test 2334ybsfhdsdjfk5sf  Tesst Pub 3
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

        try {
            file = createCsvFile(csvContents, userToken);
            message = handleJiraTicketCreation(file);
        } catch (IOException e) {
            logError("Error in file reading: ", e);
            throw e;
        }

        return message;
    }

    private File createCsvFile(String csvContents, UserToken userToken) throws IOException {
        File dir = createTempDirectory();
        if (csvContents != null) {
            return createFileWithContent(dir, csvContents, userToken);
        }
        return null;
    }

    private File createTempDirectory() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tmpDir, UIX_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    private File createFileWithContent(File dir, String content, UserToken userToken) throws IOException {
        String fileName = FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userToken.getUserId();
        File file = new File(dir, fileName);
        FileUtils.writeStringToFile(file, content);
        return file;
    }

    private Message handleJiraTicketCreation(File file) throws IOException {
        return jiraService.raiseTSUP(file);
    }

    private void logError(String message, Exception e) {
        logger.error(message, e);
    }

    public int maxSubArray(int[] nums) {
        int currentSum = nums[0]; // Start with the first element
        int maxSum = nums[0];     // Initialize maxSum with the first element
        //comment added
        
        // Traverse the array from the second element
        for (int i = 1; i < nums.length; i++) {
            // If currentSum is negative, reset to current element
            // Otherwise, add current element to currentSum
            currentSum = Math.max(nums[i], currentSum + nums[i]);
            
            // Update maxSum if currentSum is greater
            maxSum = Math.max(maxSum, currentSum);
        }
        
        return maxSum;
    }
}