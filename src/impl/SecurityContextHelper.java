package com.blueoptima.uix.controller;

import com.blueoptima.uix.security.UserToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextHelper {

    public String getUserId() {
        UserToken userToken = (UserToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userToken.getUserId();
    }
}