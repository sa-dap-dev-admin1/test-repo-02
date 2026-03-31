package com.blueoptima.uix.security;

import com.blueoptima.uix.security.UserToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class JiraSecurityUtil {

    public String getCurrentUserId() {
        UserToken userToken = (UserToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userToken.getUserId();
    }
}