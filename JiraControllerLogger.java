package com.blueoptima.uix.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JiraControllerLogger {

    private static final Logger logger = LoggerFactory.getLogger(JiraControllerLogger.class);

    public void logFileReadingError(Exception e) {
        logger.error("Error in file reading: ", e);
    }
}