package org.hsy.spring.controller.api;

import lombok.RequiredArgsConstructor;
import org.hsy.spring.entity.UserEntity;
import org.hsy.spring.repository.UserRepository;
import org.hsy.spring.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.Period;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserApiController {

    private final UserRepository userRepository;

    @GetMapping("/basic-info")
    public ResponseEntity<?> getUserBasicInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.ok(Map.of());
        }

        UserEntity user = userRepository.findByUserId(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // 나이 계산 (생년월일 → 나이)
        int age = calculateAge(user.getBirthdate());

        Map<String, Object> basicInfo = Map.of(
                "gender", user.getGender(),
                "age", age,
                "height", user.getHeight() != null ? user.getHeight() : 0,
                "weight", user.getWeight() != null ? user.getWeight() : 0
        );

        return ResponseEntity.ok(basicInfo);
    }

    private int calculateAge(LocalDate birthdate) {
        return Period.between(birthdate, LocalDate.now()).getYears();
    }

}
