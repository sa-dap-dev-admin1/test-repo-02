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

    public Message createJiraTicket(String filePath) throws IOException {
        try {
            File file = new File(filePath);
            return jiraService.raiseTSUP(file);
        } catch (IOException e) {
            logger.error("Error in file reading: ", e);
            throw e;
        }
    }
}