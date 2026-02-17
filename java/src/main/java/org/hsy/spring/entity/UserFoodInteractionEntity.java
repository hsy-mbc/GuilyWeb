package org.hsy.spring.entity;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_food_interaction", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_no", "food_no"})
})
public class UserFoodInteractionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interactionNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_no")
    private FoodInfoEntity food;

    @Column(name = "view_count", columnDefinition = "INT DEFAULT 0")
    private Integer viewCount; // 추천 노출 횟수

    @Column(name = "eat_count", columnDefinition = "INT DEFAULT 0")
    private Integer eatCount; // 실제 섭취 횟수

    @Column(name = "last_recommended_at")
    private java.time.LocalDateTime lastRecommendedAt;

    @Builder
    public UserFoodInteractionEntity(UserEntity user, FoodInfoEntity food, Integer viewCount,
                                     Integer eatCount, java.time.LocalDateTime lastRecommendedAt) {
        this.user = user;
        this.food = food;
        this.viewCount = (viewCount == null) ? 0 : viewCount;
        this.eatCount = (eatCount == null) ? 0 : eatCount;
        this.lastRecommendedAt = lastRecommendedAt;
    }

    public void increaseViewCount() {
        this.viewCount++;
        this.lastRecommendedAt = java.time.LocalDateTime.now();
    }
}