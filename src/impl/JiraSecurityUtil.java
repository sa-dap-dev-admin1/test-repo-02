package com.blueoptima.uix.controller;

import com.blueoptima.uix.security.UserToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class JiraSecurityUtil {

    public String getUserToken() {
        UserToken userToken = (UserToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userToken.getUserId();
    }
}