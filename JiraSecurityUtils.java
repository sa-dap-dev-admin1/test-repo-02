package com.blueoptima.uix.controller;

import com.blueoptima.uix.security.UserToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class JiraSecurityUtils {

    public UserToken getUserToken() {
        return (UserToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}