package org.hsy.spring.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BoardDetailResponseDTO {
    private Long postNo;
    private String title;
    private String content;
    private String authorName;
    private String authorId;
    private String categoryName;
    private String categoryCode;
    private String timeAgo;
    private int viewCount;
    private int likeCount;
    private int commentCount;

    private List<CommentResponseDTO> comments;
}
