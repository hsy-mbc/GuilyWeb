package org.hsy.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionDTO {
    private Double calories;
    private Double carbs;
    private Double protein;
    private Double fat;
    private Double fiber;
    private Double sugar;
    private Double water;
    private Double sodium;
    private Double calcium;
    private Double magnesium;
    private Double iron;
    private Double potassium;
    private Double vitaminA;
    private Double vitaminC;
    private Double vitaminD;
}