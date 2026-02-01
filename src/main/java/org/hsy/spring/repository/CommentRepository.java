package org.hsy.spring.repository;

import org.hsy.spring.entity.BoardEntity;
import org.hsy.spring.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    List<CommentEntity> findByBoardOrderByCreatedAtAsc(BoardEntity board);

    List<CommentEntity> findByBoard_PostNoOrderByCreatedAtAsc(Long postNo);

}
