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

  @RequestMapping(name = "Request to raise a ticket", value = "/v1/admin/jira/issue", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @AccessCode(PermissionsCode.DEVELOPER_READ + PermissionsCode.DEVELOPER_WRITE)
  @SkipValidationCheck
  @CSVConverter
  public Message raiseJiraTicket(@RequestBody MultipartFile data) throws IOException {
    UserToken userToken = (UserToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String csvContents = MultipartUtil.getData(data, null);
    
    // Extract file handling logic
    Optional<File> file = createCsvFile(csvContents, userToken);
    
    // Raise TSUP ticket
    return file.map(f -> {
      try {
        return jiraService.raiseTSUP(f);
      } catch (IOException e) {
        logger.error("Error in raising TSUP ticket: ", e);
        throw new RuntimeException("Failed to raise TSUP ticket", e);
      }
    }).orElseThrow(() -> new IOException("Failed to create CSV file"));
  }

  // Helper method for CSV file creation and writing
  private Optional<File> createCsvFile(String csvContents, UserToken userToken) throws IOException {
    if (csvContents == null) {
      return Optional.empty();
    }

    try {
      File dir = createOrGetDirectory();
      File file = new File(dir, generateFileName(userToken));
      FileUtils.writeStringToFile(file, csvContents);
      return Optional.of(file);
    } catch (IOException e) {
      logger.error("Error in file creation: ", e);
      throw e;
    }
  }

  // Helper method to create or get the directory
  private File createOrGetDirectory() throws IOException {
    String tmpDir = System.getProperty("java.io.tmpdir");
    File dir = new File(tmpDir, UIX_DIR);
    if (!dir.exists() && !dir.mkdir()) {
      throw new IOException("Failed to create directory: " + dir.getAbsolutePath());
    }
    return dir;
  }

  // Helper method to generate file name
  private String generateFileName(UserToken userToken) {
    return FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userToken.getUserId();
  }

  // This method seems unrelated to JiraController functionality and should be moved to a separate utility class
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