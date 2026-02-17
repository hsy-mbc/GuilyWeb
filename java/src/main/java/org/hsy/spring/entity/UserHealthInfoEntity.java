package org.hsy.spring.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_health_info")
public class UserHealthInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "health_no")
    private Long healthNo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no", nullable = false)
    private UserEntity user;

    @Column(length = 10, nullable = false)
    private String gender;

    @Column(nullable = false)
    private Double height;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private Integer age;

    @Column(name = "activity_coefficient", nullable = false)
    private Double activityCoefficient;

    @Column(name = "lifestyle_pattern", length = 50, nullable = false)
    private String lifestylePattern;

    @Column(name = "diet_goal", length = 50, nullable = false)
    private String dietGoal;

    @Column(name = "target_calories")
    private Integer targetCalories;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public UserHealthInfoEntity(Long healthNo, UserEntity user, String gender, Double height, Double weight,
                                Integer age, Double activityCoefficient, String lifestylePattern,
                                String dietGoal, Integer targetCalories) {
        this.healthNo = healthNo;
        this.user = user;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.age = age;
        this.activityCoefficient = activityCoefficient;
        this.lifestylePattern = lifestylePattern;
        this.dietGoal = dietGoal;
        this.targetCalories = targetCalories;
    }
}