package org.hsy.spring.repository;

import org.hsy.spring.entity.BoardEntity;
import org.hsy.spring.entity.LikeEntity;
import org.hsy.spring.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {

    Optional<LikeEntity> findByBoardAndUser(BoardEntity board, UserEntity user);
    long countByBoard(BoardEntity board);

}
