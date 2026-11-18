package com.coop.payments.service;

import com.coop.payments.dto.AuthRequest;
import com.coop.payments.dto.AuthResponse;
import com.coop.payments.entity.AppUser;
import com.coop.payments.exception.DuplicateResourceException;
import com.coop.payments.repository.AppUserRepository;
import com.coop.payments.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(AuthRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new DuplicateResourceException("Username already taken");
        }
        userRepository.save(AppUser.builder()
                .username(req.username())
                .password(passwordEncoder.encode(req.password()))
                .role("USER")
                .build());
        log.info("User registered username={}", req.username());
        return tokenFor(req.username());
    }

    public AuthResponse login(AuthRequest req) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        log.info("User logged in username={}", req.username());
        return tokenFor(req.username());
    }

    private AuthResponse tokenFor(String username) {
        return new AuthResponse(jwtService.generateToken(username), "Bearer", jwtService.getExpirationSeconds());
    }
}
