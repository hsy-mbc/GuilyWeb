package org.hsy.spring.service;

import lombok.RequiredArgsConstructor;
import org.hsy.spring.dto.CommentResponseDTO;
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
    public CommentResponseDTO saveComment(CommentSaveDTO dto, String userId) {
        BoardEntity board = boardRepository.findById(dto.getPostNo()).orElseThrow();
        UserEntity user = userRepository.findByUserId(userId).orElseThrow();

        CommentEntity comment = CommentEntity.builder()
                .board(board)
                .author(user)
                .content(dto.getContent())
                .build();

        commentRepository.save(comment);

        // user.getUserName() → user.getName()으로 수정
        return CommentResponseDTO.builder()
                .commentNo(comment.getCommentNo())
                .content(comment.getContent())
                .authorName(user.getName())
                .timeAgo("방금 전")
                .build();
    }

}