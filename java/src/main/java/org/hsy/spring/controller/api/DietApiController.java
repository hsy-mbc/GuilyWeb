package org.hsy.spring.controller.api;

import lombok.RequiredArgsConstructor;
import org.hsy.spring.dto.DietLogRequestDTO;
import org.hsy.spring.dto.HealthCalculateRequestDTO;
import org.hsy.spring.security.CustomUserDetails;
import org.hsy.spring.service.DietPythonService;
import org.hsy.spring.service.DietService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/diet")
@RequiredArgsConstructor
public class DietApiController {

    private final DietService dietService;
    private final DietPythonService dietPythonService;

    @PostMapping("/calculate")
    public ResponseEntity<Integer> calculateOnly(@RequestBody HealthCalculateRequestDTO request) {
        Integer targetCalories = dietService.calculateTargetCalories(request);
        return ResponseEntity.ok(targetCalories);
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveHealth(@RequestBody HealthCalculateRequestDTO request,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) {

        dietService.saveUserHealth(userDetails.getUser(), request);
        return ResponseEntity.ok(Map.of("message", "건강 정보가 저장되었습니다."));
    }

    @PostMapping("/add")
    public ResponseEntity<Long> addDiet(@RequestBody DietLogRequestDTO request,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long logNo = dietService.saveDiet(userDetails.getUser(), request);
        return ResponseEntity.ok(logNo);
    }

    @PutMapping("/{logNo}")
    public ResponseEntity<Void> updateDiet(@PathVariable Long logNo,
                                           @RequestBody DietLogRequestDTO request) {
        dietService.updateDiet(logNo, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{logNo}")
    public ResponseEntity<Void> deleteDiet(@PathVariable Long logNo) {
        dietService.deleteDiet(logNo);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/recommend/daily")
    public ResponseEntity<Map<String, Object>> getDailyRecommend(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Integer targetCal) {

        if (userDetails != null && userDetails.getUser() != null) {
            return ResponseEntity.ok(dietPythonService.getDailyPlan(userDetails.getUser().getUserNo(), null));
        } else {
            return ResponseEntity.ok(dietPythonService.getDailyPlan(null, targetCal));
        }
    }

    @GetMapping("/recommend/single")
    public ResponseEntity<Map<String, Object>> getSingleRecommend(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String mealType,
            @RequestParam(required = false) Integer targetCal) {

        if (userDetails != null && userDetails.getUser() != null) {
            return ResponseEntity.ok(dietPythonService.getSingleMeal(mealType, userDetails.getUser().getUserNo(), null));
        } else {
            return ResponseEntity.ok(dietPythonService.getSingleMeal(mealType, null, targetCal));
        }
    }


}
