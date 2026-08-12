package com.ssafy.home.member.persistence;

import com.ssafy.home.member.dto.Member;
import com.ssafy.home.member.repository.MemberEntity;
import com.ssafy.home.member.repository.MemberRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Member entity와 application DTO/command 사이를 변환하는 JPA 영속성 adapter다. */
@Repository
public class JpaMemberAdapter implements MemberPersistencePort {

    private final MemberRepository memberRepository;

    public JpaMemberAdapter(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public int insertMember(MemberInsertCommand command) {
        try {
            MemberEntity saved = memberRepository.saveAndFlush(new MemberEntity(
                    command.getEmail(),
                    command.getPasswordHash(),
                    command.getName(),
                    command.getPhone()
            ));
            command.setMemberId(saved.getMemberId());
            return 1;
        } catch (org.springframework.dao.DataIntegrityViolationException exception) {
            throw new DuplicateKeyException("member email already exists", exception);
        }
    }

    @Override
    public Optional<Member> selectById(Long memberId) {
        return memberRepository.findById(memberId).map(JpaMemberAdapter::toMember);
    }

    @Override
    public Optional<Member> selectByEmail(String email) {
        return memberRepository.findByEmail(email).map(JpaMemberAdapter::toMember);
    }

    @Override
    public List<Member> searchMembers(String keyword) {
        return memberRepository
                .findTop20ByEmailContainingIgnoreCaseOrNameContainingIgnoreCaseOrPhoneContainingIgnoreCaseOrderByMemberIdAsc(
                        keyword, keyword, keyword
                )
                .stream()
                .map(JpaMemberAdapter::toMember)
                .toList();
    }

    @Override
    public int updateCurrentMember(Long memberId, String name, String phone) {
        return memberRepository.findById(memberId)
                .map(member -> {
                    member.updateProfile(name, phone);
                    return 1;
                })
                .orElse(0);
    }

    @Override
    public int updatePassword(Long memberId, String passwordHash) {
        return memberRepository.findById(memberId)
                .map(member -> {
                    member.updatePassword(passwordHash);
                    return 1;
                })
                .orElse(0);
    }

    @Override
    public int deleteById(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            return 0;
        }
        memberRepository.deleteById(memberId);
        return 1;
    }

    private static Member toMember(MemberEntity entity) {
        return new Member(
                entity.getMemberId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getName(),
                entity.getPhone(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
