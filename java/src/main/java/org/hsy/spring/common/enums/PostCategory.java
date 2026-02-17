package org.hsy.spring.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostCategory {
    SUCCESS("성공후기"),
    RECIPE("레시피"),
    QUESTION("질문"),
    INFO("정보"),
    FREE("자유");

    private final String description;
}