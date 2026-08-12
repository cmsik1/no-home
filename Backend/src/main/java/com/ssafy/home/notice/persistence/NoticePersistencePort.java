package com.ssafy.home.notice.persistence;

import com.ssafy.home.notice.dto.Notice;

import java.util.List;
import java.util.Optional;

/** 공지 service가 요구하는 저장 계약으로 JPA entity/repository를 유스케이스 계층에서 숨긴다. */
public interface NoticePersistencePort {

    int insertNotice(NoticeInsertCommand command);
    Optional<Notice> selectById(Long noticeId);
    List<Notice> selectRecent(int limit);
    int updateNotice(Long noticeId, String title, String content);
    int deleteNotice(Long noticeId);
}
