package org.hsy.spring.service;

import lombok.RequiredArgsConstructor;
import org.hsy.spring.dto.CommentSaveDTO;
import org.hsy.spring.entity.BoardEntity;
import org.hsy.spring.entity.CommentEntity;
import org.hsy.spring.entity.UserEntity;
import org.hsy.spring.repository.BoardRepository;
import org.hsy.spring.repository.CommentRepository;
import org.hsy.spring.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional
    public void saveComment(CommentSaveDTO dto, String userId) {
        // 1. 댓글을 달 게시글이 존재하는지 확인
        BoardEntity board = boardRepository.findById(dto.getPostNo())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 2. 댓글 작성자 정보 가져오기
        UserEntity author = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 3. CommentEntity 객체 생성 및 연관관계 설정
        CommentEntity comment = CommentEntity.builder()
                .content(dto.getContent())
                .board(board)
                .author(author)
                .build();

        // 4. 댓글 저장
        commentRepository.save(comment);

        // 5. (선택사항) 게시글의 댓글 수 카운트 증가
        // BoardEntity에 commentCount 필드가 있다면 아래와 같이 업데이트합니다.
        board.setCommentCount(board.getCommentCount() + 1);
    }
}