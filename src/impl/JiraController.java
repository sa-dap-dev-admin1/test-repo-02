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

    private static final Logger logger = LoggerFactory.getLogger(JiraController.class);
    private static final String UIX_DIR = "uix_invalid_csv_files";
    private static final String TMP_DIR_PROPERTY = "java.io.tmpdir";
    private static final String FILE_NAME_SEPARATOR = "X";

    @Autowired
    private JiraService jiraService;

    @RequestMapping(name = "Request to raise a ticket", value = "/v1/admin/jira/issue", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @AccessCode(PermissionsCode.DEVELOPER_READ + PermissionsCode.DEVELOPER_WRITE)
    @SkipValidationCheck
    @CSVConverter
    public Message raiseJiraTicket(@RequestBody MultipartFile data) throws IOException {
        UserToken userToken = (UserToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String csvContents = MultipartUtil.getData(data, null);

        // Create temporary directory
        File dir = createTempDirectory();

        // Write CSV contents to file
        Optional<File> fileOptional = writeCSVToFile(csvContents, dir, userToken);

        // Handle Jira ticket creation
        return handleJiraTicketCreation(fileOptional.orElse(null));
    }

    private File createTempDirectory() {
        String tmpDir = System.getProperty(TMP_DIR_PROPERTY);
        File dir = new File(tmpDir, UIX_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    private Optional<File> writeCSVToFile(String csvContents, File dir, UserToken userToken) throws IOException {
        if (csvContents == null) {
            return Optional.empty();
        }

        File file = createFile(dir, FileSeparator.CSV_SEPARATOR.getName(), userToken);
        try {
            FileUtils.writeStringToFile(file, csvContents);
            return Optional.of(file);
        } catch (IOException e) {
            logger.error("Error writing CSV contents to file: ", e);
            throw e;
        }
    }

    private File createFile(File dir, String prefix, UserToken userToken) {
        return new File(dir, prefix + "_" + System.currentTimeMillis() + FILE_NAME_SEPARATOR + userToken.getUserId());
    }

    private Message handleJiraTicketCreation(File file) throws IOException {
        try {
            return jiraService.raiseTSUP(file);
        } catch (IOException e) {
            logger.error("Error in file reading: ", e);
            throw e;
        }
    }

    // This method seems unrelated to the main functionality of this controller.
    // Consider moving it to a separate utility class.
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