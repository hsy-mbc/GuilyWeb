package org.hsy.spring.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "food_info", indexes = {
        @Index(name = "idx_category", columnList = "main_category"),
        @Index(name = "idx_calories", columnList = "calories")
})
public class FoodInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "food_no")
    private Long foodNo;

    @Column(name = "food_code", unique = true, length = 50)
    private String foodCode; // 식약처 고유 코드

    @Column(name = "food_name", length = 100, nullable = false)
    private String foodName;

    @Column(name = "maker_name", length = 50)
    private String makerName;

    @Column(name = "main_category", length = 50)
    private String mainCategory;

    @Column(name = "sub_category", length = 50)
    private String subCategory;

    @Column(name = "is_meal")
    private Boolean isMeal; // 식사 여부

    @Column(nullable = false)
    private Double calories; // 1인분 환산 칼로리

    private Double carbs;
    private Double protein;
    private Double fat;

    @Column(name = "total_weight")
    private Integer totalWeight; // Z10500 (총 중량)

    @Column(name = "serving_size")
    private Integer servingSize; // 1회 제공량 수치

    // 알고리즘 가중치용 필드
    @Column(name = "protein_density")
    private Double proteinDensity; // 단백질 밀도

    @Column(name = "nutrient_score")
    private Double nutrientScore; // 종합 영양 점수 (비타민/무기질 밀도 합산)

    @Builder
    public FoodInfoEntity(String foodCode, String foodName, String makerName, String mainCategory,
                          String subCategory, Boolean isMeal, Double calories, Double carbs,
                          Double protein, Double fat, Integer totalWeight, Integer servingSize,
                          Double proteinDensity, Double nutrientScore) {
        this.foodCode = foodCode;
        this.foodName = foodName;
        this.makerName = makerName;
        this.mainCategory = mainCategory;
        this.subCategory = subCategory;
        this.isMeal = isMeal;
        this.calories = calories;
        this.carbs = carbs;
        this.protein = protein;
        this.fat = fat;
        this.totalWeight = totalWeight;
        this.servingSize = servingSize;
        this.proteinDensity = proteinDensity;
        this.nutrientScore = nutrientScore;
    }
}