package com.webappfinal.final_webapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.webappfinal.final_webapp.entity.NguoiDung;
import com.webappfinal.final_webapp.repository.NguoiDungRepository;
import com.webappfinal.final_webapp.service.DatabaseAuthenticationService;

@ExtendWith(MockitoExtension.class)
class DatabaseAuthenticationServiceTests {
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Test
    void acceptsActiveUserWithMatchingPassword() {
        when(nguoiDungRepository.findByUsernameIgnoreCase("student01")).thenReturn(Optional.of(user("student01", "hash123", true)));

        DatabaseAuthenticationService service = new DatabaseAuthenticationService(nguoiDungRepository, passwordEncoder);

        assertThat(service.isValidCredentials("student01", "hash123")).isTrue();
    }

    @Test
    void rejectsInactiveUser() {
        when(nguoiDungRepository.findByUsernameIgnoreCase("lecturer01")).thenReturn(Optional.of(user("lecturer01", "hash456", false)));

        DatabaseAuthenticationService service = new DatabaseAuthenticationService(nguoiDungRepository, passwordEncoder);

        assertThat(service.isValidCredentials("lecturer01", "hash456")).isFalse();
    }

    @Test
    void rejectsMissingUser() {
        when(nguoiDungRepository.findByUsernameIgnoreCase("missing")).thenReturn(Optional.empty());

        DatabaseAuthenticationService service = new DatabaseAuthenticationService(nguoiDungRepository, passwordEncoder);

        assertThat(service.isValidCredentials("missing", "anything")).isFalse();
    }

    @Test
    void acceptsBcryptPasswordForNewAccounts() {
        when(nguoiDungRepository.findByUsernameIgnoreCase("fresh"))
            .thenReturn(Optional.of(user("fresh", passwordEncoder.encode("secret123"), true)));

        DatabaseAuthenticationService service = new DatabaseAuthenticationService(nguoiDungRepository, passwordEncoder);

        assertThat(service.isValidCredentials("fresh", "secret123")).isTrue();
    }

    private NguoiDung user(String username, String passHash, Boolean trangThai) {
        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setUsername(username);
        nguoiDung.setPassHash(passHash);
        nguoiDung.setTrangThai(trangThai);
        return nguoiDung;
    }
}
