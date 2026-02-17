package org.hsy.spring.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hsy.spring.common.enums.UserRole;
import org.hsy.spring.common.enums.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_no")
    private Long userNo;

    // nullable=false와 length는 DB 테이블 스키마 생성을 위한 '제약 조건'입니다.
    @Column(name = "user_id", length = 50, nullable = false, unique = true)
    private String userId;

    // 비밀번호는 BCrypt 암호화 시 길이가 길어지므로 255 정도로 넉넉하게 잡는 것이 좋습니다. (50은 너무 작을 수 있음)
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(length = 10)
    private String gender;

    private LocalDate birthdate;

    private Double height;

    private Double weight;

    @Builder
    private UserEntity(String userId,
                       String password,
                       String name,
                       UserRole role,
                       UserStatus status,
                       String gender,
                       LocalDate birthdate,
                       Double height,
                       Double weight) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        // 생성자 레벨에서 기본값 처리
        this.role = (role != null) ? role : UserRole.CUSTOMER;
        this.status = (status != null) ? status : UserStatus.ACTIVE;
        this.gender = gender;
        this.birthdate = birthdate;
        this.height = height;
        this.weight = weight;
    }
}