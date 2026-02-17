package org.hsy.spring.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_health_log")
public class UserHealthLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "health_log_no")
    private Long healthLogNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private Double weight;

    @Column(name = "fat_percentage")
    private Double fatPercentage;

    @Column(name = "muscle_mass")
    private Double muscleMass;

    @Column(name = "target_calories")
    private Integer targetCalories;

    @Column(name = "recorded_date", nullable = false)
    private LocalDate recordedDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public UserHealthLogEntity(UserEntity user, Double weight, Double fatPercentage,
                               Double muscleMass, Integer targetCalories, LocalDate recordedDate) {
        this.user = user;
        this.weight = weight;
        this.fatPercentage = fatPercentage;
        this.muscleMass = muscleMass;
        this.targetCalories = targetCalories;
        this.recordedDate = recordedDate;
    }
}