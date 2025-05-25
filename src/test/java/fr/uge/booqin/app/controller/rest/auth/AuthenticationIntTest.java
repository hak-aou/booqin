package fr.uge.booqin.app.controller.rest.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uge.booqin.app.dto.auth.AuthRequest;
import fr.uge.booqin.app.dto.auth.RefreshRequest;
import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import fr.uge.booqin.infra.persistence.fixtures.UserFixtures;
import fr.uge.booqin.infra.security.auth.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT/*, properties = "spring.profiles.active=test"*/)

@TestPropertySource(
        locations = "/application-test.properties",
        properties = {
                "spring.profiles.active=test"
        }
)
@Transactional
public class AuthenticationIntTest {

    @Autowired
    private MockMvcTester tester;

    @Autowired
    private UserFixtures userFixtures;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String loginUrl = "/api/auth/login";
    private static final String loginAndroidUrl = "/android/auth/login";
    private static final String validPassword = "user";
    private static UserEntity validUserEntity;

    @BeforeEach
    void setUp() {
        var identity = userFixtures.createUserAuthIdentity();
        validUserEntity = userFixtures.createRandomUserWith(identity);
    }

    @Nested
    public class AuthSpaTest {

        @Test
        void shouldReturnJWTokensPairWhenAuthSuccessWithEmail() throws Exception {
            var req = createAuthRequestLogin(validUserEntity.getEmail(), validPassword).exchange();
            req.assertThat().hasStatus(HttpStatus.OK);
            req.assertThat().bodyJson().hasPath("$.accessToken");
            req.assertThat().cookies().isHttpOnly("refresh_token", true);
        }

        @Test
        void shouldReturnJWTokensPairWhenAuthSuccessWithUsername() throws Exception {
            var req = createAuthRequestLogin(validUserEntity.getUsername(), validPassword).exchange();
            req.assertThat().hasStatus(HttpStatus.OK);
            req.assertThat().bodyJson().hasPath("$.accessToken");
            req.assertThat().cookies().isHttpOnly("refresh_token", true);
        }

        @Test
        void shouldReturnUnauthorizedWhenFailedAuthentication() throws Exception {
            createAuthRequestLogin("wronguser", "wrongpassword")
                    .exchange()
                    .assertThat()
                    .hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void shouldReturnNewAccessTokenWhenRefreshTokenValid() {
            var tokens = jwtUtil.generateTokens("user", Duration.ofMinutes(15), Duration.ofDays(1));
            var refreshCookie = new Cookie("refresh_token", tokens.refreshToken());
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(false);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(3600);
            var req = tester.post().uri("/api/auth/refresh")
                    .accept(MediaType.APPLICATION_JSON)
                    .cookie(refreshCookie)
                    .exchange();
            req.assertThat().hasStatus(HttpStatus.OK);
            req.assertThat().bodyJson().hasPath("$.accessToken");
        }

        @Test
        void shouldReturnUnauthorizedWhenRefreshTokenInvalid() {
            var refreshCookie = new Cookie("refresh_token", "fake_refresh_token");
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(false);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(3600);
            tester.post().uri("/api/auth/refresh")
                    .accept(MediaType.APPLICATION_JSON)
                    .cookie(refreshCookie)
                    .exchange()
                    .assertThat()
                    .hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void shouldReturnBadRequestWhenRefreshTokenMissing() {
            tester.post().uri("/api/auth/refresh")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .assertThat()
                    .hasStatus(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    public class AuthAndroidTest {
        @Test
        void usernameshouldReturnJWTokensPairWhenAuthSuccessWithEmail() throws Exception {
            createAuthRequestLoginAndroid(validUserEntity.getEmail(), validPassword)
                    .exchange()
                    .assertThat()
                    .hasStatus(HttpStatus.OK)
                    .bodyJson().hasPath("$.accessToken")
                    .hasPath("$.refreshToken");
        }

        @Test
        void usernameshouldReturnJWTokensPairWhenAuthSuccessWithUsername() throws Exception {
            createAuthRequestLoginAndroid(validUserEntity.getUsername(), validPassword)
                    .exchange()
                    .assertThat()
                    .hasStatus(HttpStatus.OK)
                    .bodyJson().hasPath("$.accessToken")
                    .hasPath("$.refreshToken");
        }

        @Test
        void shouldReturnUnauthorizedWhenFailedAuthentication() throws Exception {
            createAuthRequestLoginAndroid("wronguser", "wrongpassword")
                    .exchange()
                    .assertThat()
                    .hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void shouldReturnNewAccessTokenWhenRefreshTokenValid() throws Exception {
            var tokens = jwtUtil.generateTokens("user", Duration.ofMinutes(15), Duration.ofDays(1));
            var req = tester.post().uri("/android/auth/refresh")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new RefreshRequest(tokens.refreshToken())))
                    .exchange();
            req.assertThat().hasStatus(HttpStatus.OK);
            req.assertThat().bodyJson().hasPath("$.accessToken");
        }

        @Test
        void shouldReturnUnauthorizedWhenRefreshTokenInvalid() throws Exception {
            tester.post().uri("/android/auth/refresh")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON).content(toJson(new RefreshRequest("fake_refresh_token")))
                    .exchange()
                    .assertThat()
                    .hasStatus(HttpStatus.UNAUTHORIZED);
        }

    }

    private String toJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    private MockMvcTester.MockMvcRequestBuilder createAuthRequestLogin(String url, String username, String password) throws JsonProcessingException {
        return tester.post().uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new AuthRequest(username, password, false)));
    }

    private MockMvcTester.MockMvcRequestBuilder createAuthRequestLogin(String username, String password) throws JsonProcessingException {
        return createAuthRequestLogin(loginUrl, username, password);
    }

    private MockMvcTester.MockMvcRequestBuilder createAuthRequestLoginAndroid(String username, String password) throws JsonProcessingException {
        return createAuthRequestLogin(loginAndroidUrl, username, password);
    }

}