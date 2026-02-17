package org.hsy.spring.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "diet_log")
public class DietLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_no")
    private Long logNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_no", nullable = false)
    private FoodInfoEntity food;

    @Column(name = "meal_type", length = 20, nullable = false)
    private String mealType; // BREAKFAST, LUNCH, DINNER, SNACK

    @Column(nullable = false)
    private Double quantity;

    @Column(name = "eat_date", nullable = false)
    private LocalDate eatDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public DietLogEntity(UserEntity user, FoodInfoEntity food, String mealType,
                         Double quantity, LocalDate eatDate) {
        this.user = user;
        this.food = food;
        this.mealType = mealType;
        this.quantity = quantity;
        this.eatDate = eatDate;
    }

    public void updateLog(FoodInfoEntity food, String mealType, Double quantity, LocalDate eatDate) {
        this.food = food;
        this.mealType = mealType;
        this.quantity = quantity;
        this.eatDate = eatDate;
    }

}