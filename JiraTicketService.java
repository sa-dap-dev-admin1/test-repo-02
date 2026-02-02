package com.blueoptima.uix.service;

import com.blueoptima.uix.dto.Message;
import com.blueoptima.uix.security.UserToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class JiraTicketService {

    private static final Logger logger = LoggerFactory.getLogger(JiraTicketService.class);

    @Autowired
    private JiraService jiraService;

    @Autowired
    private FileHandlingService fileHandlingService;

    public Message createJiraTicket(String csvContents) throws IOException {
        UserToken userToken = (UserToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        File file = null;
        Message message;

        try {
            file = fileHandlingService.createTemporaryFile(csvContents, userToken.getUserId());
            message = jiraService.raiseTSUP(file);
        } catch (IOException e) {
            logger.error("Error in file processing: ", e);
            throw e;
        } finally {
            if (file != null) {
                file.delete();
            }
        }

        return message;
    }
}