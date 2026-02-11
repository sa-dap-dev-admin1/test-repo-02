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
      logger.info("Starting to raise Jira ticket");
      UserToken userToken = (UserToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      String csvContents = MultipartUtil.getData(data,null);
      File file = null;
      Message message;

      //convert a multipart file to File
      try {
          File tempDir = createTempDirectory();
          Optional<File> optionalFile = writeCSVToFile(tempDir, csvContents, userToken);
          message = handleFileOperations(optionalFile);
      } catch (IOException e) {
          logger.error("Error in file operations: ", e);
          throw e;
      }
      // Testing UAT : 18
      return message;
  }

  private File createTempDirectory() throws IOException {
      logger.debug("Creating temporary directory");
      String tmpDir = System.getProperty("java.io.tmpdir");
      File dir = new File(tmpDir, UIX_DIR);
      // empty check here.
      if(!dir.exists() && !dir.mkdir()){
          throw new IOException("Failed to create directory: " + dir.getAbsolutePath());
      }
      return dir;
  }

  private Optional<File> writeCSVToFile(File dir, String csvContents, UserToken userToken) throws IOException {
      logger.debug("Writing CSV contents to file");
      // doing null check 
      if(csvContents == null) {
          return Optional.empty();
      }
      String fileName = createFileName(userToken);
      File file = new File(dir, fileName);
      FileUtils.writeStringToFile(file, csvContents);
      return Optional.of(file);
  }

  private String createFileName(UserToken userToken) {
      return FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userToken.getUserId();
  }

  private Message handleFileOperations(Optional<File> file) throws IOException {
      logger.debug("Handling file operations");
      if (!file.isPresent()) {
          logger.warn("No file present to process");
          return new Message("No file to process");
      }
      return jiraService.raiseTSUP(file.get());
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