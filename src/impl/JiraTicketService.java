package com.blueoptima.uix.service;

import com.blueoptima.uix.dto.Message;
import com.blueoptima.uix.security.UserToken;
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

    @Autowired
    private FileHandler fileHandler;

    public Message createJiraTicket(String csvContents, UserToken userToken) throws IOException {
        File file = null;
        try {
            file = fileHandler.createTemporaryFile(csvContents, userToken.getUserId());
            return jiraService.raiseTSUP(file);
        } catch (IOException e) {
            logger.error("Error in file processing: ", e);
            throw e;
        } finally {
            if (file != null && file.exists()) {
                file.delete();
            }
        }
    }
}