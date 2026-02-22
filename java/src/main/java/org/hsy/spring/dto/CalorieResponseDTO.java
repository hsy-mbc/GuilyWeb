package org.hsy.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalorieResponseDTO {
    private Integer totalCalories;
    private Double carbs;
    private Double protein;
    private Double fat;
    private Integer targetCarbs;
    private Integer targetProtein;
    private Integer targetFat;
}
