package org.hsy.spring.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "meal_set")
public class MealSetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long setId;

    @Column(nullable = false)
    private String setName; // "2025-01-20 석식"

    private String category; // 한식, 양식 등

    @Builder
    public MealSetEntity(String setName, String category) {
        this.setName = setName;
        this.category = category;
    }
}