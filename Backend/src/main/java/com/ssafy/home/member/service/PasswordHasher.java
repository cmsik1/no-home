package com.ssafy.home.member.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * BCrypt를 이용해 비밀번호 원문을 단방향 hash하고 로그인 입력을 검증한다.
 * Controller나 persistence 계층이 암호화 구현과 cost 정책을 알지 않게 한다.
 */
@Component
public class PasswordHasher {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String passwordHash) {
        return passwordEncoder.matches(rawPassword, passwordHash);
    }
}
