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

    @Autowired
    private JiraService jiraService;

    @Autowired
    private FileHandler fileHandler;

    private static final Logger logger = LoggerFactory.getLogger(JiraTicketCreator.class);

    public Message createJiraTicket(MultipartFile data) throws IOException {
        UserToken userToken = (UserToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String csvContents = processMultipartFile(data);
        File file = createAndWriteFile(csvContents, userToken);
        return callJiraService(file);
    }

    private String processMultipartFile(MultipartFile data) throws IOException {
        return MultipartUtil.getData(data, null);
    }

    private File createAndWriteFile(String csvContents, UserToken userToken) throws IOException {
        File dir = fileHandler.createTempDirectory();
        return fileHandler.writeStringToFile(dir, csvContents, userToken);
    }

    private Message callJiraService(File file) throws IOException {
        try {
            return jiraService.raiseTSUP(file);
        } catch (IOException e) {
            logger.error("Error in file reading: ", e);
            throw e;
        }
    }
}