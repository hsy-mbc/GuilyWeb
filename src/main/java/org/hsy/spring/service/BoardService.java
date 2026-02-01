package org.hsy.spring.service;

import org.hsy.spring.entity.LikeEntity;
import org.hsy.spring.repository.LikeRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.hsy.spring.common.enums.PostCategory;
import org.hsy.spring.common.formatter.DateTimeFormatters;
import org.hsy.spring.dto.BoardDetailResponseDTO;
import org.hsy.spring.dto.BoardListDTO;
import org.hsy.spring.dto.BoardWriteDTO;
import org.hsy.spring.dto.CommentResponseDTO;
import org.hsy.spring.entity.BoardEntity;
import org.hsy.spring.entity.CommentEntity;
import org.hsy.spring.entity.UserEntity;
import org.hsy.spring.repository.BoardRepository;
import org.hsy.spring.repository.CommentRepository;
import org.hsy.spring.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;


    @Transactional
    public void savePost(BoardWriteDTO dto, String userId) {
        UserEntity author = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        BoardEntity board = BoardEntity.builder()
                .category(dto.getCategory())
                .title(dto.getTitle())
                .content(dto.getContent())
                .author(author)
                .build();

        // 3. 리포지토리를 통해 DB에 저장
        boardRepository.save(board);

    }


    public List<BoardListDTO> getPostList(PostCategory category) {
        List<BoardEntity> entities;
        if (category == null) {
            entities = boardRepository.findAllByOrderByCreatedAtDesc();
        } else {
            entities = boardRepository.findByCategoryOrderByCreatedAtDesc(category);
        }

        return entities.stream().map(this::convertToDto).collect(Collectors.toList());
    }


    private BoardListDTO convertToDto(BoardEntity entity) {
        return BoardListDTO.builder()
                .postNo(entity.getPostNo())
                .title(entity.getTitle())
                .content(entity.getContent().length() > 50 ? entity.getContent().substring(0, 50) + "..." : entity.getContent())
                .authorName(entity.getAuthor().getName())
                .categoryName(entity.getCategory().getDescription())
                .categoryCode(entity.getCategory().name())
                .likeCount(entity.getLikeCount())
                .commentCount(entity.getCommentCount())
                .timeAgo(calculateTime(entity.getCreatedAt()))
                .build();
    }


    private String calculateTime(LocalDateTime createdAt) {
        LocalDateTime now = LocalDateTime.now();

        long seconds = ChronoUnit.SECONDS.between(createdAt, now);
        long minutes = ChronoUnit.MINUTES.between(createdAt, now);
        long hours = ChronoUnit.HOURS.between(createdAt, now);
        long days = ChronoUnit.DAYS.between(createdAt, now);

        if (seconds < 60) {
            return "방금 전";
        } else if (minutes < 60) {
            return minutes + "분 전";
        } else if (hours < 24) {
            return hours + "시간 전";
        } else if (days < 7) {
            return days + "일 전";
        } else {
            return createdAt.format(DateTimeFormatters.DEFAULT);
        }
    }


    @Transactional
    public BoardDetailResponseDTO getPostDetail(Long postNo) {

        BoardEntity board = boardRepository.findById(postNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        board.incrementViewCount();

        List<CommentEntity> comments = commentRepository.findByBoard_PostNoOrderByCreatedAtAsc(postNo);

        return convertToDetailDTO(board, comments);
    }


    private BoardDetailResponseDTO convertToDetailDTO(BoardEntity board, List<CommentEntity> comments) {
        return BoardDetailResponseDTO.builder()
                .postNo(board.getPostNo())
                .title(board.getTitle())
                .content(board.getContent())
                .authorName(board.getAuthor().getName())
                .categoryName(board.getCategory().getDescription())
                .categoryCode(board.getCategory().name().toLowerCase())
                .timeAgo(calculateTime(board.getCreatedAt()))
                .viewCount(board.getViewCount())
                .likeCount(board.getLikes().size())
                .commentCount(comments.size())
                .comments(comments.stream()
                        .map(c -> CommentResponseDTO.builder()
                                .commentNo(c.getCommentNo())
                                .content(c.getContent())
                                .authorName(c.getAuthor().getName())
                                .timeAgo(calculateTime(c.getCreatedAt()))
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public int toggleLike(Long postNo, String userId) {
        BoardEntity board = boardRepository.findById(postNo).orElseThrow();
        UserEntity user = userRepository.findByUserId(userId).orElseThrow();

        Optional<LikeEntity> existingLike = likeRepository.findByBoardAndUser(board, user);

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            board.setLikeCount(board.getLikeCount() - 1);
            return board.getLikeCount();
        } else {
            LikeEntity like = LikeEntity.builder().board(board).user(user).build();
            likeRepository.save(like);
            board.setLikeCount(board.getLikeCount() + 1);
            return board.getLikeCount();
        }
    }

}
