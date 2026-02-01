package org.hsy.spring.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentResponseDTO {
    private Long commentNo;
    private String content;
    private String authorName;
    private String timeAgo;
}