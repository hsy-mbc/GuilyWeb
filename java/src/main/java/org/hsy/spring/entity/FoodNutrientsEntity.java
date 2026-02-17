package org.hsy.spring.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "food_nutrients")
public class FoodNutrientsEntity {

    @Id
    private Long foodNo;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "food_no")
    private FoodInfoEntity foodInfo;

    private Double water;
    private Double sugar;
    private Double fiber;
    private Double sodium;
    private Double calcium;
    private Double magnesium;
    private Double iron;
    private Double potassium;
    private Double vitaminA;
    private Double vitaminC;
    private Double vitaminD;

    @Builder
    public FoodNutrientsEntity(FoodInfoEntity foodInfo, Double water, Double sugar, Double fiber,
                               Double sodium, Double calcium, Double magnesium, Double iron,
                               Double potassium, Double vitaminA, Double vitaminC, Double vitaminD) {
        this.foodInfo = foodInfo;
        this.water = water;
        this.sugar = sugar;
        this.fiber = fiber;
        this.sodium = sodium;
        this.calcium = calcium;
        this.magnesium = magnesium;
        this.iron = iron;
        this.potassium = potassium;
        this.vitaminA = vitaminA;
        this.vitaminC = vitaminC;
        this.vitaminD = vitaminD;
    }
}