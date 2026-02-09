package com.blueoptima.uix.controller;

import com.blueoptima.iam.dto.PermissionsCode;
import com.blueoptima.uix.SkipValidationCheck;
import com.blueoptima.uix.annotations.CSVConverter;
import com.blueoptima.uix.dto.Message;
import com.blueoptima.uix.service.JiraService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class JiraController {

    @Autowired
    private JiraService jiraService;

    @Autowired
    private JiraFileHandler fileHandler;

    @Autowired
    private JiraSecurityManager securityManager;

    private static final Logger logger = LoggerFactory.getLogger(JiraController.class);

    @RequestMapping(name = "Request to raise a ticket", value = "/v1/admin/jira/issue", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @AccessCode(PermissionsCode.DEVELOPER_READ + PermissionsCode.DEVELOPER_WRITE)
    @SkipValidationCheck
    @CSVConverter
    public Message raiseJiraTicket(@RequestBody MultipartFile data) throws IOException {
        securityManager.validateUserPermissions();
        String csvContents = fileHandler.extractCsvContents(data);
        return createJiraTicket(csvContents);
    }

    private Message createJiraTicket(String csvContents) throws IOException {
        try {
            return jiraService.raiseTSUP(fileHandler.createTemporaryFile(csvContents));
        } catch (IOException e) {
            logger.error("Error in file processing: ", e);
            throw e;
        }
    }
}