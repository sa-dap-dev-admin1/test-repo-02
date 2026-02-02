package com.blueoptima.uix.controller;

import com.blueoptima.uix.dto.Message;
import com.blueoptima.uix.security.UserToken;
import com.blueoptima.uix.service.JiraService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Component
public class JiraServiceWrapper {

    private static final Logger logger = LoggerFactory.getLogger(JiraServiceWrapper.class);

    @Autowired
    private JiraService jiraService;

    @Autowired
    private JiraFileProcessor jiraFileProcessor;

    public Message processJiraTicket(MultipartFile data, UserToken userToken) throws IOException {
        File file = null;
        try {
            file = jiraFileProcessor.processFile(data, userToken);
            return jiraService.raiseTSUP(file);
        } catch (IOException e) {
            logger.error("Error processing Jira ticket: ", e);
            throw e;
        } finally {
            if (file != null && file.exists()) {
                file.delete();
            }
        }
    }
}