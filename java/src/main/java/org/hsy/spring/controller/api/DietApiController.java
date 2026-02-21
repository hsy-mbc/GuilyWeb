package org.hsy.spring.controller.api;

import lombok.RequiredArgsConstructor;
import org.hsy.spring.dto.*;
import org.hsy.spring.entity.DietLogEntity;
import org.hsy.spring.entity.FoodInfoEntity;
import org.hsy.spring.security.CustomUserDetails;
import org.hsy.spring.service.DietPythonService;
import org.hsy.spring.service.DietService;
import org.hsy.spring.service.NutritionTargetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diet")
@RequiredArgsConstructor
public class DietApiController {

    private final DietService dietService;
    private final DietPythonService dietPythonService;
    private final NutritionTargetService nutritionTargetService;

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
    public ResponseEntity<?> addDiet(@RequestBody DietLogRequestDTO request,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long logNo = dietService.saveDTODiet(userDetails.getUser(), request);
        return ResponseEntity.ok(Map.of("logNo", logNo, "message", "식사가 추가되었습니다."));
    }

    @PutMapping("/{logNo}")
    public ResponseEntity<Void> updateDiet(@PathVariable Long logNo,
                                           @RequestBody DietLogRequestDTO request) {
        dietService.updateDiet(logNo, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{logNo}")
    public ResponseEntity<?> deleteMeal(
            @PathVariable Long logNo,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        dietService.deleteDiet(logNo, userDetails.getUser());

        return ResponseEntity.ok(Map.of("message", "식사가 삭제되었습니다."));
    }

    @GetMapping("/recommend/daily")
    public ResponseEntity<Map<String, Object>> getDailyRecommend(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Integer targetCal) {

        Map<String, Object> recommendations;

        if (userDetails != null && userDetails.getUser() != null) {
            recommendations = dietPythonService.getDailyPlan(userDetails.getUser().getUserNo(), null);

            List<DietLogEntity> todayLogs = dietService.getTodayDietLogs(userDetails.getUser().getUserNo());

            if (todayLogs.isEmpty()) {
                dietService.saveRecommendedMeals(userDetails.getUser(), recommendations);
            }

        } else {
            recommendations = dietPythonService.getDailyPlan(null, targetCal);
        }

        return ResponseEntity.ok(recommendations);
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

    @GetMapping("/meals/today")
    public ResponseEntity<Map<String, Object>> getTodayMeals(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.ok(Map.of(
                    "BREAKFAST", List.of(),
                    "LUNCH", List.of(),
                    "DINNER", List.of()
            ));
        }

        Map<String, Object> meals = dietService.getTodayMealsGrouped(userDetails.getUser().getUserNo());
        return ResponseEntity.ok(meals);
    }

    @GetMapping("/goal")
    public ResponseEntity<?> getUserGoal(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.ok(Map.of());
        }

        GoalResponseDTO goal = dietService.getUserGoal(userDetails.getUser());

        if (goal == null) {
            return ResponseEntity.ok(Map.of());
        }

        return ResponseEntity.ok(goal);
    }

    @GetMapping("/calories/today")
    public ResponseEntity<?> getTodayCalories(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.ok(CalorieResponseDTO.builder()
                    .totalCalories(0)
                    .carbs(0.0)
                    .protein(0.0)
                    .fat(0.0)
                    .targetCarbs(0)
                    .targetProtein(0)
                    .targetFat(0)
                    .build());
        }

        CalorieResponseDTO calories = dietService.getTodayCalories(userDetails.getUser());

        return ResponseEntity.ok(calories);
    }

    @GetMapping("/meal/{mealType}")
    public ResponseEntity<MealDetailResponseDTO> getMealDetail(
            @PathVariable String mealType,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.ok(MealDetailResponseDTO.builder()
                    .totalNutrition(new NutritionDTO())
                    .targetNutrition(new NutritionDTO())
                    .foods(List.of())
                    .build());
        }

        // DietService에서 식사 상세 정보 조회
        MealDetailResponseDTO response = dietService.getMealDetail(
                userDetails.getUser().getUserNo(),
                mealType.toUpperCase()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchFood(@RequestParam String query, @RequestParam(defaultValue = "20") int limit) {
        List<FoodInfoEntity> foods = dietService.searchByName(query, limit);

        List<FoodSearchDTO> foodList = foods.stream()
                .map(food -> FoodSearchDTO.builder()
                        .foodNo(food.getFoodNo())
                        .foodName(food.getFoodName())
                        .calories(food.getCalories() != null ? food.getCalories() : 0.0)
                        .carbs(food.getCarbs() != null ? food.getCarbs() : 0.0)
                        .protein(food.getProtein() != null ? food.getProtein() : 0.0)
                        .fat(food.getFat() != null ? food.getFat() : 0.0)
                        .build())
                .toList();

        return ResponseEntity.ok(Map.of("foods", foodList));
    }


}