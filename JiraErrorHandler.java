package com.blueoptima.uix.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JiraErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(JiraErrorHandler.class);

    public void logError(String message, Exception e) {
        logger.error(message, e);
    }
}