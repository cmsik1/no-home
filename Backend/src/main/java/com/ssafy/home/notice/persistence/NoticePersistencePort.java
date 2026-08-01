package com.ssafy.home.notice.persistence;

import com.ssafy.home.notice.dto.Notice;

import java.util.List;
import java.util.Optional;

public interface NoticePersistencePort {

    int insertNotice(NoticeInsertCommand command);
    Optional<Notice> selectById(Long noticeId);
    List<Notice> selectRecent(int limit);
    int updateNotice(Long noticeId, String title, String content);
    int deleteNotice(Long noticeId);
}
