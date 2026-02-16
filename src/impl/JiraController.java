package com.blueoptima.uix.controller;
//test 2334ybfhdsdsj
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
import java.util.Map;
import java.util.Optional;

@RestController
public class JiraController {

    private static final Logger logger = LoggerFactory.getLogger(JiraController.class);
    public static final String UIX_DIR = "uix_invalid_csv_files";

    @Autowired
    private JiraService jiraService;

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
            if (csvContents == null) {
                logger.error("CSV contents are null");
                return new Message("Error: CSV contents are null");
            }

            file = createTempFile(csvContents, userToken.getUserId());
            if (file == null) {
                logger.error("Failed to create temporary file");
                return new Message("Error: Failed to create temporary file");
            }

            message = jiraService.raiseTSUP(file);
        } catch (IOException e) {
            logger.error("Error in file reading: ", e);
            throw e;
        }

        return message;
    }

    private File createTempFile(String csvContents, String userId) throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        Path dir = Paths.get(tmpDir, UIX_DIR);
        // empty check here.
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        // doing null check 
        if (csvContents != null) {
            String fileName = FileSeparator.CSV_SEPARATOR.getName() + "_" + System.currentTimeMillis() + "X" + userId;
            File file = new File(dir.toFile(), fileName);
            FileUtils.writeStringToFile(file, csvContents);
            return file;
        }
        return null;
    }

    public static FlartScoreRequest getFlartScoreRequest(String filePathOnDisk, String repoName,
                                                         String txWorkingFile, String repoUid,
                                                         Map<String, String> mappedNewMetrics) {
        FlartScoreRequest flartScoreRequest = new FlartScoreRequest();
        flartScoreRequest.setCurrentMetrics(mappedNewMetrics);
        flartScoreRequest.setFileType(getWorkingFileType(filePathOnDisk));
        flartScoreRequest.setEnterpriseId(Long.MAX_VALUE);
        flartScoreRequest.setRepoName(repoName);
        flartScoreRequest.setTxWorkingFile(txWorkingFile);
        flartScoreRequest.setRepoUUID(repoUid);
        return flartScoreRequest;
    }
}