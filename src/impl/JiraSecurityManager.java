package com.blueoptima.uix.controller;

import com.blueoptima.uix.security.UserToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class JiraSecurityManager {

    public void validateUserPermissions() {
        UserToken userToken = (UserToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Additional permission validation logic can be added here
    }

    public String getCurrentUserId() {
        UserToken userToken = (UserToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userToken.getUserId();
    }
}