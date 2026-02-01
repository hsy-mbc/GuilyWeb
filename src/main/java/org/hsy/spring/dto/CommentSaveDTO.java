package org.hsy.spring.dto;

import lombok.Data;

@Data
public class CommentSaveDTO {
    private Long postNo;
    private String content;
}
