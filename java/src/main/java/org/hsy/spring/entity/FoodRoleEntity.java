package org.hsy.spring.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "food_role")
public class FoodRoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_no", nullable = false)
    private FoodInfoEntity food;

    @Column(nullable = false)
    private String role;

    @Builder
    public FoodRoleEntity(FoodInfoEntity food, String role) {
        this.food = food;
        this.role = role;
    }
}