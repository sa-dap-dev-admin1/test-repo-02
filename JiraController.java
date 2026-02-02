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
    private static final String FILE_NAME_FORMAT = "%s_%dX%s";

    @RequestMapping(name = "Request to raise a ticket", value = "/v1/admin/jira/issue", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @AccessCode(PermissionsCode.DEVELOPER_READ + PermissionsCode.DEVELOPER_WRITE)
    @SkipValidationCheck
    @CSVConverter
    public Message raiseJiraTicket(@RequestBody MultipartFile data) throws IOException {
        UserToken userToken = extractUserToken();
        String csvContents = MultipartUtil.getData(data, null);
        File file = null;

        try {
            File dir = createTempDirectory();
            if (csvContents != null) {
                file = writeCSVToFile(dir, csvContents, userToken);
            }
            return raiseJiraIssue(file);
        } catch (IOException e) {
            logger.error("Error in file reading: ", e);
            throw e;
        }
    }

    private UserToken extractUserToken() {
        return (UserToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private File createTempDirectory() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tmpDir, UIX_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    private File writeCSVToFile(File dir, String csvContents, UserToken userToken) throws IOException {
        String fileName = String.format(FILE_NAME_FORMAT, FileSeparator.CSV_SEPARATOR.getName(), System.currentTimeMillis(), userToken.getUserId());
        File file = new File(dir, fileName);
        FileUtils.writeStringToFile(file, csvContents);
        return file;
    }

    private Message raiseJiraIssue(File file) {
        return jiraService.raiseTSUP(file);
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