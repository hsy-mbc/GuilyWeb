package org.hsy.spring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hsy.spring.common.enums.PostCategory;

@Data
public class BoardWriteDTO {

    @NotNull(message = "카테고리를 선택해주세요.")
    private PostCategory category;

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;
}