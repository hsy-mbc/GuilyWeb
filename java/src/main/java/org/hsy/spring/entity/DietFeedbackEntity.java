package org.hsy.spring.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "diet_feedback")
public class DietFeedbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_no")
    private Long feedbackNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no", nullable = false)
    private UserEntity user;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Column(name = "total_calories", nullable = false)
    private Integer totalCalories;

    @Column(name = "diet_score")
    private Integer dietScore;

    @Column(name = "ai_comment", columnDefinition = "TEXT")
    private String aiComment;

    @Column(length = 20)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public DietFeedbackEntity(UserEntity user, LocalDate targetDate, Integer totalCalories,
                              Integer dietScore, String aiComment, String status) {
        this.user = user;
        this.targetDate = targetDate;
        this.totalCalories = totalCalories;
        this.dietScore = dietScore;
        this.aiComment = aiComment;
        this.status = status;
    }
}