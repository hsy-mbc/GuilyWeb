package org.hsy.spring.repository;

import org.hsy.spring.common.enums.PostCategory;
import org.hsy.spring.entity.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
    List<BoardEntity> findAllByOrderByCreatedAtDesc();
    List<BoardEntity> findByCategoryOrderByCreatedAtDesc(PostCategory category);
    // 전체 검색 (제목 + 내용)
    List<BoardEntity> findByTitleContainingOrContentContainingOrderByCreatedAtDesc(
            String titleKeyword, String contentKeyword);
    // 카테고리 + 검색
    List<BoardEntity> findByCategoryAndTitleContainingOrCategoryAndContentContainingOrderByCreatedAtDesc(
            PostCategory category1, String titleKeyword,
            PostCategory category2, String contentKeyword);

}