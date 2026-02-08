package com.blueoptima.uix.controller;

import com.blueoptima.uix.dto.Message;
import com.blueoptima.uix.service.JiraService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class JiraTicketCreator {

    @Autowired
    private JiraService jiraService;

    private static final Logger logger = LoggerFactory.getLogger(JiraTicketCreator.class);

    public Message createTicket(File file) {
        try {
            return jiraService.raiseTSUP(file);
        } catch (Exception e) {
            logger.error("Error in creating Jira ticket: ", e);
            return new Message("Error creating Jira ticket: " + e.getMessage());
        }
    }
}