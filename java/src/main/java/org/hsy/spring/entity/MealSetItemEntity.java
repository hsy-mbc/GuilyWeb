package org.hsy.spring.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "meal_set_item")
public class MealSetItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 식단 세트인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id", nullable = false)
    private MealSetEntity mealSet;

    // 어떤 음식인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_no", nullable = false)
    private FoodInfoEntity foodInfo;

    // 역할 (탄수 / 단백질 / 반찬 등)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private FoodRoleEntity role;

    @Builder
    public MealSetItemEntity(MealSetEntity mealSet, FoodInfoEntity foodInfo, FoodRoleEntity role) {
        this.mealSet = mealSet;
        this.foodInfo = foodInfo;
        this.role = role;
    }
}