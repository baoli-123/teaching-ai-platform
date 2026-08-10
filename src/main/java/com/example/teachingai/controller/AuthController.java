package com.example.teachingai.controller;

import com.example.teachingai.dto.ApiResponse;
import com.example.teachingai.dto.AuthResponse;
import com.example.teachingai.dto.LoginRequest;
import com.example.teachingai.dto.RefreshRequest;
import com.example.teachingai.entity.AppUser;
import com.example.teachingai.security.JwtService;
import com.example.teachingai.security.UserDetailsServiceImpl;
import com.example.teachingai.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        AppUser user = principal.getUser();
        AuthResponse response = new AuthResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                user.getUsername(),
                user.getDisplayName(),
                user.getRole()
        );
        return ApiResponse.ok(response);
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@RequestBody RefreshRequest request) {
        var claims = jwtService.parseToken(request.getRefreshToken());
        String username = claims.getSubject();
        if (!jwtService.isRefreshTokenValid(username, request.getRefreshToken())) {
            return ApiResponse.error("refresh token invalid");
        }
        UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserByUsername(username);
        AppUser user = principal.getUser();
        AuthResponse response = new AuthResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                user.getUsername(),
                user.getDisplayName(),
                user.getRole()
        );
        return ApiResponse.ok(response);
    }

    @GetMapping("/me")
    public ApiResponse<AppUser> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(principal.getUser());
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal != null) {
            jwtService.revokeRefreshToken(principal.getUsername());
        }
        return ApiResponse.ok("logged out");
    }
}
