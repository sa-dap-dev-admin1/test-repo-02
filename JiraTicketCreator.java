package com.blueoptima.uix.service;

import com.blueoptima.uix.dto.Message;
import com.blueoptima.uix.security.UserToken;
import com.blueoptima.uix.util.MultipartUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class JiraTicketCreator {

    private static final Logger logger = LoggerFactory.getLogger(JiraTicketCreator.class);

    @Autowired
    private JiraService jiraService;

    @Autowired
    private FileHandler fileHandler;

    public Message createTicket(MultipartFile data) throws IOException {
        UserToken userToken = (UserToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String csvContents = MultipartUtil.getData(data, null);
        File file = null;
        Message message;

        try {
            file = fileHandler.createFile(csvContents, userToken.getUserId());
            message = jiraService.raiseTSUP(file);
        } catch (IOException e) {
            logger.error("Error in file processing: ", e);
            throw e;
        } finally {
            if (file != null && file.exists()) {
                file.delete();
            }
        }

        return message;
    }
}