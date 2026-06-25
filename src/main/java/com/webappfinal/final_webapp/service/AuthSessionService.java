package com.webappfinal.final_webapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.webappfinal.final_webapp.util.CookieUtility;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthSessionService {
    public static final String LOGIN_COOKIE = "loggedInUser";

    private static final int ONE_DAY_SECONDS = 60 * 60 * 24;

    private final DatabaseAuthenticationService databaseAuthenticationService;
    private final boolean databaseAuthEnabled;
    private final String demoUsername;
    private final String demoPassword;

    public AuthSessionService(
            DatabaseAuthenticationService databaseAuthenticationService,
            @Value("${app.auth.database.enabled:true}") boolean databaseAuthEnabled,
            @Value("${app.auth.demo.username:admin1}") String demoUsername,
            @Value("${app.auth.demo.password:1}") String demoPassword) {
        this.databaseAuthenticationService = databaseAuthenticationService;
        this.databaseAuthEnabled = databaseAuthEnabled;
        this.demoUsername = demoUsername;
        this.demoPassword = demoPassword;
    }

    public boolean isAuthenticated(HttpServletRequest request) {
        String username = getAuthenticatedUsername(request);
        return username != null && !username.isBlank();
    }

    public String getAuthenticatedUsername(HttpServletRequest request) {
        return CookieUtility.getCookieValue(request, LOGIN_COOKIE);
    }

    public boolean isValidCredentials(String username, String password) {
        if (databaseAuthEnabled && databaseAuthenticationService.isValidCredentials(username, password)) {
            return true;
        }

        return demoUsername.equals(username) && demoPassword.equals(password);
    }

    public void login(HttpServletResponse response, String username) {
        CookieUtility.addCookie(response, LOGIN_COOKIE, username, ONE_DAY_SECONDS);
    }

    public void logout(HttpServletResponse response) {
        CookieUtility.deleteCookie(response, LOGIN_COOKIE);
    }
}
