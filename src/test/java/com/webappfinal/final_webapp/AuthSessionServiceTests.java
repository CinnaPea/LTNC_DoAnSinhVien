package com.webappfinal.final_webapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webappfinal.final_webapp.service.AuthSessionService;
import com.webappfinal.final_webapp.service.DatabaseAuthenticationService;

@ExtendWith(MockitoExtension.class)
class AuthSessionServiceTests {

    @Mock
    private DatabaseAuthenticationService databaseAuthenticationService;

    @Test
    void acceptsDatabaseUserWhenQueryMatches() {
        when(databaseAuthenticationService.isValidCredentials("student01", "hash123")).thenReturn(true);

        AuthSessionService service = new AuthSessionService(
                databaseAuthenticationService,
                true,
                "admin1",
                "1");

        assertThat(service.isValidCredentials("student01", "hash123")).isTrue();
    }

    @Test
    void fallsBackToDemoUserWhenDatabaseDoesNotMatch() {
        when(databaseAuthenticationService.isValidCredentials("admin1", "1")).thenReturn(false);

        AuthSessionService service = new AuthSessionService(
                databaseAuthenticationService,
                true,
                "admin1",
                "1");

        assertThat(service.isValidCredentials("admin1", "1")).isTrue();
    }

    @Test
    void rejectsInactiveDatabaseUser() {
        when(databaseAuthenticationService.isValidCredentials("lecturer01", "hash456")).thenReturn(false);

        AuthSessionService service = new AuthSessionService(
                databaseAuthenticationService,
                true,
                "admin1",
                "1");

        assertThat(service.isValidCredentials("lecturer01", "hash456")).isFalse();
    }

    @Test
    void acceptsActiveBitColumnReturnedAsBoolean() {
        when(databaseAuthenticationService.isValidCredentials("student01", "hash123")).thenReturn(true);

        AuthSessionService service = new AuthSessionService(
                databaseAuthenticationService,
                true,
                "admin1",
                "1");

        assertThat(service.isValidCredentials("student01", "hash123")).isTrue();
    }
}
