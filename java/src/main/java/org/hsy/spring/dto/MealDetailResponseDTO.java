package org.hsy.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealDetailResponseDTO {
    private NutritionDTO totalNutrition;
    private NutritionDTO targetNutrition;
    private List<FoodItemDTO> foods;
}