package com.netflix.backend.controller;

import com.netflix.backend.dto.AuthRequest;
import com.netflix.backend.dto.AuthResponse;
import com.netflix.backend.model.User;
import com.netflix.backend.repository.UserRepository;
import com.netflix.backend.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody AuthRequest request, HttpServletResponse response) {
        log.info("Signup request received for user: {}", request.getEmail());
        AuthResponse authResponse = authService.signup(request);
        setCookie(response, authResponse.getToken());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request, HttpServletResponse response) {
        log.info("Login request received for user: {}", request.getEmail());
        AuthResponse authResponse = authService.login(request);
        setCookie(response, authResponse.getToken());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpServletResponse response) {
        clearCookie(response);
        return ResponseEntity.ok(AuthResponse.builder().success(true).message("Logged out successfully").build());
    }

    private void setCookie(HttpServletResponse response, String token) {
        // We use a manual header because the standard Servlet Cookie API
        // doesn't support 'SameSite' attributes directly in older versions.
        String cookieHeader = String.format(
                "jwt-netflix=%s; Path=/; Max-Age=%d; HttpOnly; Secure; SameSite=None",
                token,
                15 * 24 * 60 * 60 // 15 days
        );

        response.addHeader("Set-Cookie", cookieHeader);
    }

    private void clearCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt-netflix", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    @GetMapping("/authCheck")
    public ResponseEntity<AuthResponse> authCheck(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            log.warn("authCheck: UserDetails is null - Unauthorized");
            return ResponseEntity.status(401)
                    .body(AuthResponse.builder().success(false).message("Unauthorized").build());
        }
        log.info("authCheck success for user: {}", userDetails.getUsername());
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElse(null);
        return ResponseEntity.ok(AuthResponse.builder().success(true).user(user).build());
    }
}
