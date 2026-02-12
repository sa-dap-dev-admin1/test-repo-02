package com.blueoptima.uix.controller;

import com.blueoptima.iam.dto.PermissionsCode;
import com.blueoptima.uix.SkipValidationCheck;
import com.blueoptima.uix.annotations.CSVConverter;
import com.blueoptima.uix.dto.Message;
import com.blueoptima.uix.security.auth.AccessCode;
import com.blueoptima.uix.service.JiraService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
public class JiraController {

    private final JiraService jiraService;
    private final JiraFileHandler fileHandler;
    private final JiraSecurityUtil securityUtil;

    public JiraController(JiraService jiraService, JiraFileHandler fileHandler, JiraSecurityUtil securityUtil) {
        this.jiraService = jiraService;
        this.fileHandler = fileHandler;
        this.securityUtil = securityUtil;
    }

    @RequestMapping(name = "Request to raise a ticket", value = "/v1/admin/jira/issue", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @AccessCode(PermissionsCode.DEVELOPER_READ + PermissionsCode.DEVELOPER_WRITE)
    @SkipValidationCheck
    @CSVConverter
    public Message raiseJiraTicket(@RequestBody MultipartFile data) throws IOException {
        String userId = getUserId();
        String csvContents = fileHandler.extractCsvContents(data);
        return createJiraTicket(csvContents, userId);
    }

    private String getUserId() {
        return Optional.ofNullable(securityUtil.getCurrentUserId())
                .orElseThrow(() -> new SecurityException("User ID not found"));
    }

    private Message createJiraTicket(String csvContents, String userId) throws IOException {
        try {
            return jiraService.raiseTSUP(fileHandler.createTempFile(csvContents, userId));
        } catch (IOException e) {
            throw new IOException("Error creating Jira ticket", e);
        }
    }
}