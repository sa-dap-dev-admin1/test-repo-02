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

@RestController
public class JiraController {

    @Autowired
    private JiraService jiraService;

    private static final Logger logger = LoggerFactory.getLogger(JiraController.class);

    public static final String UIX_DIR = "uix_invalid_csv_files";
    private static final String TMP_DIR_PROPERTY = "java.io.tmpdir";

    /**
     * Raises a Jira ticket based on the provided CSV data.
     *
     * @param data The MultipartFile containing CSV data
     * @return A Message object with the result of the operation
     * @throws IOException If there's an error reading or writing the file
     */
    @RequestMapping(name = "Request to raise a ticket", value = "/v1/admin/jira/issue", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @AccessCode(PermissionsCode.DEVELOPER_READ + PermissionsCode.DEVELOPER_WRITE)
    @SkipValidationCheck
    @CSVConverter
    public Message raiseJiraTicket(@RequestBody MultipartFile data) throws IOException {
        UserToken userToken = (UserToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String csvContents = MultipartUtil.getData(data, null);

        if (csvContents == null) {
            logger.error("CSV contents are null");
            throw new IOException("Invalid CSV data");
        }

        File file = createTemporaryFile(userToken.getUserId(), csvContents);
        return jiraService.raiseTSUP(file);
    }

    private File createTemporaryFile(String userId, String csvContents) throws IOException {
        File dir = createTempDirectory();
        String fileName = generateFileName(userId);
        File file = new File(dir, fileName);
        FileUtils.writeStringToFile(file, csvContents);
        return file;
    }

    private File createTempDirectory() throws IOException {
        String tmpDir = System.getProperty(TMP_DIR_PROPERTY);
        File dir = new File(tmpDir, UIX_DIR);
        if (!dir.exists() && !dir.mkdir()) {
            throw new IOException("Failed to create temporary directory");
        }
        return dir;
    }

    private String generateFileName(String userId) {
        return FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userId;
    }

    /**
     * Calculates the maximum subarray sum in the given array.
     *
     * @param nums The input array of integers
     * @return The maximum subarray sum
     */
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