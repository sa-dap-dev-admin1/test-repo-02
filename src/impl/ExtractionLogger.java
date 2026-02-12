package com.blueoptima.connectors.scr;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.File;

public class ExtractionLogger {
    private final Logger logger;
    private final String logFilePath;

    public ExtractionLogger(String logsDir, String requestID) {
        this.logger = LogManager.getLogger(SCR.class);
        this.logFilePath = logsDir + File.separator + "extraction_" + requestID + ".log";
        configureLogger();
    }

    private void configureLogger() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(SCR.class.getName());
        
        // Set default log level to INFO
        loggerConfig.setLevel(Level.INFO);
        
        // Create and configure FileAppender
        PatternLayout layout = PatternLayout.newBuilder()
                .withPattern("%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n")
                .build();

        FileAppender fileAppender = FileAppender.newBuilder()
                .setName("ExtractionFileAppender")
                .setFileName(logFilePath)
                .setLayout(layout)
                .setConfiguration(config)
                .build();

        fileAppender.start();
        
        // Add file appender to logger configuration
        loggerConfig.addAppender(fileAppender, Level.INFO, null);
        
        context.updateLoggers();
    }

    public void setLogLevel(Level level) {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(SCR.class.getName());
        loggerConfig.setLevel(level);
        context.updateLoggers();
    }

    public void info(String message) {
        logger.info(message);
    }

    public void error(String message) {
        logger.error(message);
    }

    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    public void warn(String message) {
        logger.warn(message);
    }

    public void debug(String message) {
        logger.debug(message);
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public void close() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(SCR.class.getName());
        
        FileAppender fileAppender = (FileAppender) loggerConfig.getAppenders().get("ExtractionFileAppender");
        if (fileAppender != null) {
            fileAppender.stop();
            loggerConfig.removeAppender("ExtractionFileAppender");
        }
        
        context.updateLoggers();
    }
}