package com.ssafy.home.notice.persistence;

import com.ssafy.home.notice.dto.Notice;
import com.ssafy.home.test.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaNoticeAdapter.class)
class JpaNoticeAdapterTest extends PostgresIntegrationTest {

    @Autowired
    private NoticePersistencePort noticeMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void insertSelectUpdateAndDeleteNotice() {
        jdbcTemplate.update("DELETE FROM notices");
        Long memberId = insertMember();
        NoticeInsertCommand command = new NoticeInsertCommand(memberId, "Before", "Content");

        int inserted = noticeMapper.insertNotice(command);
        int updated = noticeMapper.updateNotice(command.getNoticeId(), "After", "Changed");
        List<Notice> notices = noticeMapper.selectRecent(10);

        assertThat(inserted).isEqualTo(1);
        assertThat(command.getNoticeId()).isNotNull();
        assertThat(updated).isEqualTo(1);
        assertThat(notices).extracting(Notice::title).containsExactly("After");
        assertThat(noticeMapper.selectById(command.getNoticeId())).isPresent()
                .get()
                .extracting(Notice::content)
                .isEqualTo("Changed");
        int deleted = noticeMapper.deleteNotice(command.getNoticeId());
        assertThat(deleted).isEqualTo(1);
        assertThat(noticeMapper.selectById(command.getNoticeId())).isEmpty();
    }

    private Long insertMember() {
        jdbcTemplate.update("""
                INSERT INTO members (email, password_hash, name, phone)
                VALUES ('notice@example.com', 'hash', 'Notice User', '010')
                """);
        return jdbcTemplate.queryForObject("SELECT member_id FROM members WHERE email = 'notice@example.com'", Long.class);
    }
}
