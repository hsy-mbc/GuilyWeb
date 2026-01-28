package org.hsy.spring.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hsy.spring.entity.UserEntity;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSignupDTO {

    @NotBlank(message = "아이디는 필수입니다.")
    @Size(min = 4, max = 20, message = "아이디는 4~20자 사이여야 합니다.")
    private String userId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$", message = "비밀번호는 영문, 숫자 포함 8자 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "성별을 선택해주세요.")
    private String gender;

    @NotNull(message = "생년월일을 입력해주세요.")
    @Past(message = "유효하지 않은 날짜입니다.")
    private LocalDate birthdate;

    private Double height;
    private Double weight;

    // DTO를 엔티티로 변환해주는 편의 메서드
    public UserEntity toEntity() {
        return UserEntity.builder()
                .userId(userId)
                .password(password)
                .name(name)
                .gender(gender)
                .birthdate(birthdate)
                .height(height)
                .weight(weight)
                .build();
    }
}