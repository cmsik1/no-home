package com.ssafy.home.notice.persistence;

import com.ssafy.home.notice.dto.Notice;
import com.ssafy.home.notice.repository.NoticeEntity;
import com.ssafy.home.notice.repository.NoticeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaNoticeAdapter implements NoticePersistencePort {

    private final NoticeRepository noticeRepository;

    public JpaNoticeAdapter(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    @Override
    public int insertNotice(NoticeInsertCommand command) {
        NoticeEntity saved = noticeRepository.saveAndFlush(new NoticeEntity(
                command.getMemberId(), command.getTitle(), command.getContent()
        ));
        command.setNoticeId(saved.getNoticeId());
        return 1;
    }

    @Override
    public Optional<Notice> selectById(Long noticeId) {
        return noticeRepository.findById(noticeId).map(JpaNoticeAdapter::toNotice);
    }

    @Override
    public List<Notice> selectRecent(int limit) {
        return noticeRepository.findAllByOrderByCreatedAtDescNoticeIdDesc(PageRequest.of(0, limit))
                .stream()
                .map(JpaNoticeAdapter::toNotice)
                .toList();
    }

    @Override
    public int updateNotice(Long noticeId, String title, String content) {
        return noticeRepository.findById(noticeId)
                .map(notice -> {
                    notice.update(title, content);
                    return 1;
                })
                .orElse(0);
    }

    @Override
    public int deleteNotice(Long noticeId) {
        if (!noticeRepository.existsById(noticeId)) {
            return 0;
        }
        noticeRepository.deleteById(noticeId);
        return 1;
    }

    private static Notice toNotice(NoticeEntity entity) {
        return new Notice(
                entity.getNoticeId(), entity.getMemberId(), entity.getTitle(), entity.getContent(),
                entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }
}
