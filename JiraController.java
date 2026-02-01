package com.blueoptima.uix.controller;

import com.blueoptima.iam.dto.PermissionsCode;
import com.blueoptima.uix.SkipValidationCheck;
import com.blueoptima.uix.annotations.CSVConverter;
import com.blueoptima.uix.dto.Message;
import com.blueoptima.uix.security.UserToken;
import com.blueoptima.uix.security.auth.AccessCode;
import com.blueoptima.uix.service.JiraService;
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

    @Autowired
    private JiraFileHandler jiraFileHandler;

    private static final Logger logger = LoggerFactory.getLogger(JiraController.class);

    /**
     * Raises a Jira ticket based on the provided CSV file.
     *
     * @param data The MultipartFile containing CSV data
     * @return A Message object with the result of the operation
     * @throws IOException If there's an error in file processing
     */
    @RequestMapping(name = "Request to raise a ticket", value = "/v1/admin/jira/issue", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @AccessCode(PermissionsCode.DEVELOPER_READ + PermissionsCode.DEVELOPER_WRITE)
    @SkipValidationCheck
    @CSVConverter
    public Message raiseJiraTicket(@RequestBody MultipartFile data) throws IOException {
        UserToken userToken = getUserToken();
        File file = jiraFileHandler.processCSVFile(data, userToken);
        return jiraService.raiseTSUP(file);
    }

    private UserToken getUserToken() {
        return (UserToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}