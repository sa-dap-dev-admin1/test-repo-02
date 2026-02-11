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

  @RequestMapping(name = "Request to raise a ticket", value = "/v1/admin/jira/issue", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @AccessCode(PermissionsCode.DEVELOPER_READ + PermissionsCode.DEVELOPER_WRITE)
  @SkipValidationCheck
  @CSVConverter
  public Message raiseJiraTicket(@RequestBody MultipartFile data) throws IOException {

      UserToken userToken = (UserToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      String csvContents = MultipartUtil.getData(data,null);
      File file = null;
      Message message;

      //convert a multipart file to File
      try {
          file = createCSVFile(csvContents, userToken.getUserId());
          message = createJiraTicket(file);
      } catch (IOException e) {
          logger.error("Error in file reading: ",e);
          throw e;
      }
      // Testing UAT : 20

      return message;
  }

  private File createCSVFile(String csvContents, long userId) throws IOException {
      if(csvContents == null || csvContents.isEmpty()) {
          throw new IllegalArgumentException("CSV contents cannot be null or empty");
      }

      Path dir = createAndGetDirectory();
      String fileName = FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userId;
      Path filePath = dir.resolve(fileName);

      try {
          return Files.write(filePath, csvContents.getBytes()).toFile();
      } catch (IOException e) {
          logger.error("Error in file handling: ", e);
          throw e;
      }
  }

  private Path createAndGetDirectory() throws IOException {
      String tmpDir = System.getProperty("java.io.tmpdir");
      Path dir = Paths.get(tmpDir, UIX_DIR);
      // empty check here.
      if(!Files.exists(dir)){
          try {
              Files.createDirectories(dir);
          } catch (IOException e) {
              logger.error("Error creating directory: ", e);
              throw e;
          }
      }
      return dir;
  }

  private Message createJiraTicket(File file) {
      try {
          return jiraService.raiseTSUP(file);
      } catch (Exception e) {
          logger.error("Error creating Jira ticket: ", e);
          throw new RuntimeException("Failed to create Jira ticket", e);
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