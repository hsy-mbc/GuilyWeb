package org.hsy.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalResponseDTO {
    private Integer targetCalories;
    private String dietGoal;
    private String aiComment;
}
