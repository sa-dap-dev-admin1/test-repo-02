package com.blueoptima.uix.controller;

import com.blueoptima.uix.dto.Message;
import com.blueoptima.uix.service.JiraService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class JiraTicketService {

    private static final Logger logger = LoggerFactory.getLogger(JiraTicketService.class);

    @Autowired
    private JiraService jiraService;

    public Message createJiraTicket(String csvContents) throws IOException {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("jira_ticket_", ".csv");
            org.apache.commons.io.FileUtils.writeStringToFile(tempFile, csvContents);
            return jiraService.raiseTSUP(tempFile);
        } catch (IOException e) {
            logger.error("Error in file processing: ", e);
            throw e;
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}