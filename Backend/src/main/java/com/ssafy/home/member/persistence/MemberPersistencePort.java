package com.ssafy.home.member.persistence;

import com.ssafy.home.member.dto.Member;

import java.util.List;
import java.util.Optional;

public interface MemberPersistencePort {

    int insertMember(MemberInsertCommand command);
    Optional<Member> selectById(Long memberId);
    Optional<Member> selectByEmail(String email);
    List<Member> searchMembers(String keyword);
    int updateCurrentMember(Long memberId, String name, String phone);
    int updatePassword(Long memberId, String passwordHash);
    int deleteById(Long memberId);
}
