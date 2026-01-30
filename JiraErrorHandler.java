package com.blueoptima.uix.controller;

import com.blueoptima.uix.dto.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JiraErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(JiraErrorHandler.class);

    public Message handleFileError(IOException e) {
        logger.error("Error in file reading: ", e);
        return new Message("Error occurred while processing the file");
    }
}