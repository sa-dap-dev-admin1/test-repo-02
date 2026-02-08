package com.blueoptima.uix.controller;

import com.blueoptima.uix.dto.Message;
import com.blueoptima.uix.service.JiraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class JiraServiceIntegrator {

    @Autowired
    private JiraService jiraService;

    public Message raiseTicket(String filePath) {
        File file = new File(filePath);
        return jiraService.raiseTSUP(file);
    }
}