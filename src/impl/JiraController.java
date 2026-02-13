package com.blueoptima.uix.controller;
//test 2334ybfhdsdj
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
            file = createFile(csvContents, userToken);
            message = jiraService.raiseTSUP(file);
        } catch (IOException e) {
            logger.error("Error in file reading: ", e);
            throw e;
        }

        return message;
    }

    private File createFile(String csvContents, UserToken userToken) throws IOException {
        // empty check here.
        Path tempDir = Files.createDirectories(Paths.get(System.getProperty("java.io.tmpdir"), UIX_DIR));
        
        // doing null check 
        if (csvContents != null) {
            String fileName = String.format("%s_%dX%d", FileSeparator.CSV_SEPARATOR.getName(), System.currentTimeMillis(), userToken.getUserId());
            Path filePath = tempDir.resolve(fileName);
            FileUtils.writeStringToFile(filePath.toFile(), csvContents);
            return filePath.toFile();
        }
        return null;
    }
}