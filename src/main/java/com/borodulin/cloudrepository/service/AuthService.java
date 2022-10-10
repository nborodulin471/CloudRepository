package com.borodulin.cloudrepository.service;

import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

@Component
public class AuthService {

    public void logout(HttpServletRequest request, Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(null);
        try {
            request.logout();
        } catch (ServletException e) {
            throw new AuthorizationServiceException("Не удалось разлогиниться");
        }
        request.getSession().invalidate();
        authentication.setAuthenticated(false);
    }
}
