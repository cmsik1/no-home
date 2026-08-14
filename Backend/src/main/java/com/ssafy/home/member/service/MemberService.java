package com.ssafy.home.member.service;

import com.ssafy.home.common.feature.DeploymentFeaturePolicy;
import com.ssafy.home.member.dto.Member;
import com.ssafy.home.member.dto.MemberResponse;
import com.ssafy.home.member.dto.MemberSignupRequest;
import com.ssafy.home.member.dto.MemberUpdateRequest;
import com.ssafy.home.member.dto.PasswordResetRequest;
import com.ssafy.home.member.persistence.MemberInsertCommand;
import com.ssafy.home.member.persistence.MemberPersistencePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 회원 가입·조회·수정과 관리자 검색 규칙을 담당한다.
 * 비밀번호 원문은 {@link PasswordHasher}에서 hash/검증하고 persistence port에는 저장 가능한 command만 전달한다.
 */
@Service
public class MemberService {

    private final MemberPersistencePort memberPersistencePort;
    private final PasswordHasher passwordHasher;
    private final Set<String> adminEmails;
    private final DeploymentFeaturePolicy featurePolicy;

    @Autowired
    public MemberService(
            MemberPersistencePort memberPersistencePort,
            PasswordHasher passwordHasher,
            @Value("${notice.admin-emails:}") String adminEmails,
            DeploymentFeaturePolicy featurePolicy
    ) {
        this.memberPersistencePort = memberPersistencePort;
        this.passwordHasher = passwordHasher;
        this.adminEmails = parseAdminEmails(adminEmails);
        this.featurePolicy = featurePolicy;
    }

    public MemberService(MemberPersistencePort memberPersistencePort, PasswordHasher passwordHasher,
                         String adminEmails) {
        this(memberPersistencePort, passwordHasher, adminEmails, DeploymentFeaturePolicy.allEnabled());
    }

    @Transactional
    public MemberResponse signup(MemberSignupRequest request) {
        String email = required(request == null ? null : request.email(), "email is required.");
        String password = required(request == null ? null : request.password(), "password is required.");
        String name = required(request == null ? null : request.name(), "name is required.");
        String phone = trimToNull(request == null ? null : request.phone());

        memberPersistencePort.selectByEmail(email).ifPresent(member -> {
            throw new MemberException(MemberErrorCode.DUPLICATE_EMAIL, "email already exists.");
        });

        MemberInsertCommand command = new MemberInsertCommand(email, passwordHasher.hash(password), name, phone);
        memberPersistencePort.insertMember(command);
        return findResponseById(command.getMemberId());
    }

    public MemberResponse login(String email, String password) {
        String normalizedEmail = required(email, "email is required.");
        String rawPassword = required(password, "password is required.");
        Member member = memberPersistencePort.selectByEmail(normalizedEmail)
                .orElseThrow(() -> invalidCredentials());
        if (!passwordHasher.matches(rawPassword, member.passwordHash())) {
            throw invalidCredentials();
        }
        return MemberResponse.from(member);
    }

    @Transactional
    public MemberResponse resetPassword(PasswordResetRequest request) {
        String email = required(request == null ? null : request.email(), "email is required.");
        String name = required(request == null ? null : request.name(), "name is required.");
        String phone = trimToNull(request == null ? null : request.phone());
        String newPassword = required(request == null ? null : request.newPassword(), "newPassword is required.");

        Member member = memberPersistencePort.selectByEmail(email)
                .orElseThrow(() -> invalidCredentials());
        if (!member.name().equals(name) || !sameNullable(member.phone(), phone)) {
            throw invalidCredentials();
        }

        int updated = memberPersistencePort.updatePassword(member.memberId(), passwordHasher.hash(newPassword));
        if (updated == 0) {
            throw new MemberException(MemberErrorCode.NOT_FOUND, "member not found.");
        }
        return findResponseById(member.memberId());
    }

    public MemberResponse findCurrentMember(Long memberId) {
        requireMemberId(memberId);
        return findResponseById(memberId);
    }

    public List<MemberResponse> searchMembers(Long currentMemberId, String keyword) {
        featurePolicy.requireMemberSearchEnabled();
        requireAdminMemberId(currentMemberId);
        String normalizedKeyword = required(keyword, "keyword is required.");
        return memberPersistencePort.searchMembers(normalizedKeyword).stream()
                .map(MemberResponse::from)
                .toList();
    }

    @Transactional
    public MemberResponse updateCurrentMember(Long memberId, MemberUpdateRequest request) {
        requireMemberId(memberId);
        String name = required(request == null ? null : request.name(), "name is required.");
        String phone = trimToNull(request == null ? null : request.phone());
        int updated = memberPersistencePort.updateCurrentMember(memberId, name, phone);
        if (updated == 0) {
            throw new MemberException(MemberErrorCode.NOT_FOUND, "member not found.");
        }
        return findResponseById(memberId);
    }

    @Transactional
    public void deleteCurrentMember(Long memberId) {
        requireMemberId(memberId);
        int deleted = memberPersistencePort.deleteById(memberId);
        if (deleted == 0) {
            throw new MemberException(MemberErrorCode.NOT_FOUND, "member not found.");
        }
    }

    private MemberResponse findResponseById(Long memberId) {
        Member member = memberPersistencePort.selectById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND, "member not found."));
        return MemberResponse.from(member);
    }

    private static void requireMemberId(Long memberId) {
        if (memberId == null) {
            throw new MemberException(MemberErrorCode.UNAUTHENTICATED, "login is required.");
        }
    }

    private void requireAdminMemberId(Long memberId) {
        requireMemberId(memberId);
        if (!isAdmin(memberId)) {
            throw new MemberException(MemberErrorCode.FORBIDDEN, "admin permission is required.");
        }
    }

    private boolean isAdmin(Long memberId) {
        if (adminEmails.isEmpty()) {
            return false;
        }
        return memberPersistencePort.selectById(memberId)
                .map(Member::email)
                .map(MemberService::normalizeEmail)
                .filter(email -> !email.isBlank())
                .map(adminEmails::contains)
                .orElse(false);
    }

    private static Set<String> parseAdminEmails(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(value.split(","))
                .map(MemberService::normalizeEmail)
                .filter(email -> !email.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    private static String normalizeEmail(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private static MemberException invalidCredentials() {
        return new MemberException(MemberErrorCode.INVALID_CREDENTIALS, "invalid email or password.");
    }

    private static String required(String value, String message) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new MemberException(MemberErrorCode.VALIDATION, message);
        }
        return trimmed;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean sameNullable(String left, String right) {
        String normalizedLeft = trimToNull(left);
        String normalizedRight = trimToNull(right);
        if (normalizedLeft == null) {
            return normalizedRight == null;
        }
        return normalizedLeft.equals(normalizedRight);
    }
}
