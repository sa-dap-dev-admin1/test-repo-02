package com.blueoptima.uix.controller;
//test Generated PRId
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
    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");
    private static final String FILE_NAME_PREFIX = FileSeparator.CSV_SEPARATOR.getName() + "_";

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
            String tmpDir = System.getProperty("java.io.tmpdir");
            File dir = new File(tmpDir, UIX_DIR);
            // empty check here.
            if (!dir.exists()) {
                dir.mkdir();
            }
            // doing null check 
            if (csvContents != null) {
                file = createFile(dir, userToken.getUserId());
                writeContentToFile(file, csvContents);
            }

            message = raiseTSUPTicket(file);

        } catch (IOException e) {
            logger.error("Error in file reading: ", e);
            throw e;
        }

        return message;
    }

    private File createFile(File dir, String userId) {
        String fileName = FILE_NAME_PREFIX + System.currentTimeMillis() + "X" + userId;
        return new File(dir, fileName);
    }

    private void writeContentToFile(File file, String content) throws IOException {
        try {
            FileUtils.writeStringToFile(file, content);
            logger.info("File created successfully: {}", file.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error writing to file: {}", file.getAbsolutePath(), e);
            throw e;
        }
    }

    private Message raiseTSUPTicket(File file) {
        try {
            return jiraService.raiseTSUP(file);
        } catch (Exception e) {
            logger.error("Error raising TSUP ticket: ", e);
            return new Message("Failed to raise TSUP ticket");
        }
    }
}