package org.hsy.spring.repository;

import org.hsy.spring.common.enums.PostCategory;
import org.hsy.spring.entity.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
    List<BoardEntity> findAllByOrderByCreatedAtDesc();
    List<BoardEntity> findByCategoryOrderByCreatedAtDesc(PostCategory category);

}