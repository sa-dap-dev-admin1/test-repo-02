package com.blueoptima.uix.service;

import com.blueoptima.uix.dto.Message;
import com.blueoptima.uix.security.UserToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class JiraTicketService {

    @Autowired
    private FileHandlingService fileHandlingService;

    @Autowired
    private CSVProcessingService csvProcessingService;

    @Autowired
    private JiraService jiraService;

    @Autowired
    private JiraControllerLogger logger;

    public Message createJiraTicket(MultipartFile data) throws IOException {
        UserToken userToken = (UserToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String csvContents = csvProcessingService.extractCSVContents(data);
        File file = null;
        Message message;

        try {
            file = fileHandlingService.createTemporaryFile(csvContents, userToken.getUserId());
            message = jiraService.raiseTSUP(file);
        } catch (IOException e) {
            logger.logFileReadingError(e);
            throw e;
        } finally {
            fileHandlingService.cleanupTemporaryFile(file);
        }

        return message;
    }
}