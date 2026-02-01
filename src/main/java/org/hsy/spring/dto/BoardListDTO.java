package org.hsy.spring.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BoardListDTO {
    private Long postNo;
    private String title;
    private String content;
    private String authorName;
    private String categoryName;
    private String categoryCode;
    private int likeCount;
    private int commentCount;
    private String timeAgo;
}
