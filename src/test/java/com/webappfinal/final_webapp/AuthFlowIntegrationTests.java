package com.webappfinal.final_webapp;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.webappfinal.final_webapp.service.AuthSessionService;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void dashboardRedirectsToLoginWhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void signupPageLoadsForLoggedOutUsers() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signup"))
                .andExpect(model().attributeExists("signupForm"));
    }

    @Test
    void loginWithValidCredentialsCreatesCookieAndRedirectsToDashboard() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "admin1")
                        .param("password", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(cookie().value(AuthSessionService.LOGIN_COOKIE, "admin1"))
                .andExpect(cookie().httpOnly(AuthSessionService.LOGIN_COOKIE, true))
                .andExpect(cookie().path(AuthSessionService.LOGIN_COOKIE, "/"));
    }

    @Test
    void loginWithInvalidCredentialsReturnsLoginPageWithError() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "wrong")
                        .param("password", "creds"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attribute("enteredUsername", equalTo("wrong")))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void authenticatedUserCanOpenDashboard() throws Exception {
        mockMvc.perform(get("/dashboard")
                        .cookie(new Cookie(AuthSessionService.LOGIN_COOKIE, "admin1")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));
    }

    @Test
    void logoutClearsCookieAndRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(cookie().value(AuthSessionService.LOGIN_COOKIE, ""))
                .andExpect(cookie().maxAge(AuthSessionService.LOGIN_COOKIE, 0))
                .andExpect(header().string("Set-Cookie", containsString(AuthSessionService.LOGIN_COOKIE + "=")));
    }
}
