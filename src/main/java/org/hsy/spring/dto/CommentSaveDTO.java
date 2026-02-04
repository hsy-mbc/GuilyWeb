package org.hsy.spring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentSaveDTO {

    @NotNull(message = "게시글 번호가 필요합니다.")
    private Long postNo;

    @NotBlank(message = "댓글 내용을 입력해주세요.")
    private String content;
}