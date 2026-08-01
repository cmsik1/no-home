package com.ssafy.home.member.persistence;

import com.ssafy.home.member.dto.Member;
import com.ssafy.home.test.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaMemberAdapter.class, JpaRefreshTokenAdapter.class})
class JpaMemberAdapterTest extends PostgresIntegrationTest {

    @Autowired
    private MemberPersistencePort memberMapper;

    @Autowired
    private RefreshTokenPersistencePort refreshTokenMapper;

    @Test
    void insertMemberCanBeSelectedByEmail() {
        MemberInsertCommand command = insertMember("email-select@example.com", "hash-email", "Email User", "010-1111-1111");

        Optional<Member> selected = memberMapper.selectByEmail("email-select@example.com");

        assertThat(command.getMemberId()).isNotNull();
        assertThat(selected).isPresent();
        assertThat(selected.get().memberId()).isEqualTo(command.getMemberId());
        assertThat(selected.get().email()).isEqualTo("email-select@example.com");
        assertThat(selected.get().passwordHash()).isEqualTo("hash-email");
        assertThat(selected.get().name()).isEqualTo("Email User");
        assertThat(selected.get().phone()).isEqualTo("010-1111-1111");
    }

    @Test
    void insertMemberCanBeSelectedById() {
        MemberInsertCommand command = insertMember("id-select@example.com", "hash-id", "Id User", null);

        Optional<Member> selected = memberMapper.selectById(command.getMemberId());

        assertThat(selected).isPresent();
        assertThat(selected.get().memberId()).isEqualTo(command.getMemberId());
        assertThat(selected.get().email()).isEqualTo("id-select@example.com");
        assertThat(selected.get().passwordHash()).isEqualTo("hash-id");
        assertThat(selected.get().phone()).isNull();
    }

    @Test
    void updateCurrentMemberChangesEditableFields() {
        MemberInsertCommand command = insertMember("update@example.com", "hash-update", "Before", "010-before");

        int updated = memberMapper.updateCurrentMember(command.getMemberId(), "After", "010-after");

        Optional<Member> selected = memberMapper.selectById(command.getMemberId());
        assertThat(updated).isEqualTo(1);
        assertThat(selected).isPresent();
        assertThat(selected.get().name()).isEqualTo("After");
        assertThat(selected.get().phone()).isEqualTo("010-after");
        assertThat(selected.get().email()).isEqualTo("update@example.com");
        assertThat(selected.get().passwordHash()).isEqualTo("hash-update");
    }

    @Test
    void updatePasswordChangesOnlyPasswordHash() {
        MemberInsertCommand command = insertMember("password@example.com", "hash-before", "Password User", "010");

        int updated = memberMapper.updatePassword(command.getMemberId(), "hash-after");

        Optional<Member> selected = memberMapper.selectById(command.getMemberId());
        assertThat(updated).isEqualTo(1);
        assertThat(selected).isPresent();
        assertThat(selected.get().passwordHash()).isEqualTo("hash-after");
        assertThat(selected.get().email()).isEqualTo("password@example.com");
        assertThat(selected.get().name()).isEqualTo("Password User");
        assertThat(selected.get().phone()).isEqualTo("010");
    }

    @Test
    void searchMembersFindsByEmailNameOrPhone() {
        insertMember("alpha@example.com", "hash-alpha", "Alpha User", "010-1111");
        insertMember("beta@example.com", "hash-beta", "Beta User", "010-2222");

        assertThat(memberMapper.searchMembers("alpha")).extracting(Member::email)
                .containsExactly("alpha@example.com");
        assertThat(memberMapper.searchMembers("Beta")).extracting(Member::email)
                .containsExactly("beta@example.com");
        assertThat(memberMapper.searchMembers("2222")).extracting(Member::email)
                .containsExactly("beta@example.com");
    }

    @Test
    void deleteByIdRemovesMember() {
        MemberInsertCommand command = insertMember("delete@example.com", "hash-delete", "Delete User", null);

        int deleted = memberMapper.deleteById(command.getMemberId());

        assertThat(deleted).isEqualTo(1);
        assertThat(memberMapper.selectById(command.getMemberId())).isEmpty();
        assertThat(memberMapper.selectByEmail("delete@example.com")).isEmpty();
    }

    @Test
    void duplicateEmailInsertFailsByUniqueConstraint() {
        insertMember("duplicate@example.com", "hash-one", "One", null);

        MemberInsertCommand duplicate = new MemberInsertCommand("duplicate@example.com", "hash-two", "Two", null);

        assertThatThrownBy(() -> memberMapper.insertMember(duplicate))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void refreshTokenCanBeStoredRotatedAndRevoked() {
        MemberInsertCommand command = insertMember("refresh@example.com", "hash", "Refresh User", null);
        java.time.LocalDateTime expiresAt = java.time.LocalDateTime.now().plusDays(7);

        assertThat(refreshTokenMapper.upsert(command.getMemberId(), "hash-one", expiresAt)).isEqualTo(1);
        assertThat(refreshTokenMapper.rotate(command.getMemberId(), "hash-one", "hash-two", expiresAt)).isEqualTo(1);
        assertThat(refreshTokenMapper.rotate(command.getMemberId(), "hash-one", "hash-three", expiresAt)).isZero();
        assertThat(refreshTokenMapper.deleteByTokenHash("hash-two")).isEqualTo(1);
    }

    private MemberInsertCommand insertMember(String email, String passwordHash, String name, String phone) {
        MemberInsertCommand command = new MemberInsertCommand(email, passwordHash, name, phone);
        int inserted = memberMapper.insertMember(command);
        assertThat(inserted).isEqualTo(1);
        assertThat(command.getMemberId()).isNotNull();
        return command;
    }
}
