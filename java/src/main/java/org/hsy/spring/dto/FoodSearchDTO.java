package org.hsy.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodSearchDTO {
    private Long foodNo;
    private String foodName;
    private Double calories;
    private Double carbs;
    private Double protein;
    private Double fat;
}