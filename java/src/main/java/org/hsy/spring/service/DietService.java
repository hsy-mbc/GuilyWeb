package org.hsy.spring.service;

import org.hsy.spring.dto.*;
import org.hsy.spring.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.hsy.spring.entity.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DietService {

    private final UserHealthInfoRepository healthInfoRepository;
    private final UserHealthLogRepository healthLogRepository;
    private final DietLogRepository dietLogRepository;
    private final FoodInfoRepository foodInfoRepository;
    private final DietPythonService dietPythonService;
    private final UserRepository userRepository;
    private final NutritionTargetService nutritionTargetService;
    private final FoodNutrientsRepository foodNutrientsRepository;

    public Integer calculateTargetCalories(HealthCalculateRequestDTO request) {
        // BMR 계산 (Mifflin-St Jeor 공식)
        double bmr;
        if ("MALE".equals(request.getGender())) {
            bmr = (10 * request.getWeight()) + (6.25 * request.getHeight()) - (5 * request.getAge()) + 5;
        } else {
            bmr = (10 * request.getWeight()) + (6.25 * request.getHeight()) - (5 * request.getAge()) - 161;
        }

        // TDEE 계산 (BMR * 활동 계수)
        double tdee = bmr * request.getActivityCoefficient();

        double targetCalories = tdee;
        if ("LOSS".equals(request.getDietGoal())) {
            targetCalories -= 500; // 다이어트: -500kcal
        } else if ("GAIN".equals(request.getDietGoal())) {
            targetCalories += 300; // 벌크업: +300kcal
        }

        return (int) Math.round(targetCalories);
    }


    @Transactional
    public void saveUserHealth(UserEntity user, HealthCalculateRequestDTO request) {
        Integer targetCal = calculateTargetCalories(request);

        Optional<UserHealthInfoEntity> existingInfo = healthInfoRepository.findByUser(user);
        Long existingHealthNo = existingInfo.map(UserHealthInfoEntity::getHealthNo).orElse(null);

        UserHealthInfoEntity updatedInfo = UserHealthInfoEntity.builder()
                .healthNo(existingHealthNo)
                .user(user)
                .gender(request.getGender())
                .height(request.getHeight())
                .weight(request.getWeight())
                .age(request.getAge())
                .activityCoefficient(request.getActivityCoefficient())
                .lifestylePattern(request.getLifestylePattern())
                .dietGoal(request.getDietGoal())
                .targetCalories(targetCal)
                .build();

        healthInfoRepository.save(updatedInfo);

        UserHealthLogEntity log = UserHealthLogEntity.builder()
                .user(user)
                .weight(request.getWeight())
                .targetCalories(targetCal)
                .recordedDate(LocalDate.now())
                .build();

        healthLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTodayMealsGrouped(Long userNo) {
        List<DietLogEntity> logs = getTodayDietLogs(userNo);

        Map<String, List<Map<String, Object>>> grouped = new HashMap<>();
        grouped.put("BREAKFAST", new ArrayList<>());
        grouped.put("LUNCH", new ArrayList<>());
        grouped.put("DINNER", new ArrayList<>());

        for (DietLogEntity log : logs) {
            FoodInfoEntity food = log.getFood();

            Map<String, Object> foodData = Map.of(
                    "logNo", log.getLogNo(),
                    "food_no", food.getFoodNo(),
                    "food_name", food.getFoodName(),
                    "calories", (int) (food.getCalories() * log.getQuantity()),
                    "quantity", log.getQuantity()
            );

            grouped.get(log.getMealType()).add(foodData);
        }

        return Map.of(
                "BREAKFAST", grouped.get("BREAKFAST"),
                "LUNCH", grouped.get("LUNCH"),
                "DINNER", grouped.get("DINNER")
        );
    }

    @Transactional
    public Long saveDiet(UserEntity user, Long foodNo, String mealType, Double quantity, LocalDate eatDate) {
        FoodInfoEntity food = foodInfoRepository.findById(foodNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 음식입니다."));

        DietLogEntity log = DietLogEntity.builder()
                .user(user)
                .food(food)
                .mealType(mealType)
                .quantity(quantity)
                .eatDate(eatDate)
                .build();

        return dietLogRepository.save(log).getLogNo();
    }

    @Transactional
    public Long saveDTODiet(UserEntity user, DietLogRequestDTO request) {
        return saveDiet(user, request.getFoodNo(), request.getMealType(),
                request.getQuantity(), request.getEatDate());
    }

    @Transactional
    public void saveRecommendedMeals(UserEntity user, Map<String, Object> recommendations) {
        LocalDate today = LocalDate.now();

        Map<String, Object> data = (Map<String, Object>) recommendations.get("data");
        if (data == null) return;

        saveMealType(user, data, "BREAKFAST", today);
        saveMealType(user, data, "LUNCH", today);
        saveMealType(user, data, "DINNER", today);
    }

    private void saveMealType(UserEntity user, Map<String, Object> data, String mealType, LocalDate eatDate) {
        List<Map<String, Object>> foods = (List<Map<String, Object>>) data.get(mealType);

        if (foods == null || foods.isEmpty()) return;

        for (Map<String, Object> foodData : foods) {
            Long foodNo = ((Number) foodData.get("food_no")).longValue();

            saveDiet(user, foodNo, mealType, 1.0, eatDate);
        }
    }

    @Transactional
    public void updateDiet(Long logNo, DietLogRequestDTO request) {
        DietLogEntity log = dietLogRepository.findById(logNo)
                .orElseThrow(() -> new IllegalArgumentException("기록을 찾을 수 없습니다."));

        FoodInfoEntity food = foodInfoRepository.findById(request.getFoodNo())
                .orElseThrow(() -> new IllegalArgumentException("음식 정보가 없습니다."));

        log.updateLog(food, request.getMealType(), request.getQuantity(), request.getEatDate());
    }


    @Transactional
    public void deleteDiet(Long logNo, UserEntity user) {
        DietLogEntity log = dietLogRepository.findById(logNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 식단 기록입니다."));

        if (!log.getUser().getUserNo().equals(user.getUserNo())) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        dietLogRepository.delete(log);
    }

    public boolean hasHealthInfo(UserEntity user) {
        if (user == null) return false;
        return healthInfoRepository.existsByUser(user);
    }

    @Transactional(readOnly = true)
    public List<DietLogEntity> getTodayDietLogs(Long userNo) {
        LocalDate today = LocalDate.now();

        return dietLogRepository.findTodayDietWithFood(userNo, today);
    }

    public GoalResponseDTO getUserGoal(UserEntity user) {
        UserHealthInfoEntity healthInfo = healthInfoRepository.findByUser(user)
                .orElse(null);

        if (healthInfo == null) {
            return null;
        }

        String aiComment = generateAiComment(user, healthInfo);
        System.out.println(aiComment);

        return GoalResponseDTO.builder()
                .targetCalories(healthInfo.getTargetCalories())
                .dietGoal(healthInfo.getDietGoal())
                .aiComment(aiComment)
                .build();
    }

    public CalorieResponseDTO getTodayCalories(UserEntity user) {
        LocalDate today = LocalDate.now();
        List<DietLogEntity> todayLogs = dietLogRepository.findByUserAndEatDate(user, today);

        int totalCalories = 0;
        double totalCarbs = 0.0;
        double totalProtein = 0.0;
        double totalFat = 0.0;

        for (DietLogEntity log : todayLogs) {
            FoodInfoEntity food = log.getFood();
            double quantity = log.getQuantity();

            totalCalories += (int) (food.getCalories() * quantity);
            totalCarbs += food.getCarbs() * quantity;
            totalProtein += food.getProtein() * quantity;
            totalFat += food.getFat() * quantity;
        }

        UserHealthInfoEntity healthInfo = healthInfoRepository.findByUser(user)
                .orElse(null);

        int targetCarbs = 260;
        int targetProtein = 100;
        int targetFat = 60;

        if (healthInfo != null && healthInfo.getTargetCalories() != null) {
            int targetCal = healthInfo.getTargetCalories();
            targetCarbs = (int) ((targetCal * 0.5) / 4);
            targetProtein = (int) ((targetCal * 0.25) / 4);
            targetFat = (int) ((targetCal * 0.25) / 9);
        }

        return CalorieResponseDTO.builder()
                .totalCalories(totalCalories)
                .carbs(totalCarbs)
                .protein(totalProtein)
                .fat(totalFat)
                .targetCarbs(targetCarbs)
                .targetProtein(targetProtein)
                .targetFat(targetFat)
                .build();
    }

    private String generateAiComment(UserEntity user, UserHealthInfoEntity healthInfo) {
        try {
            CalorieResponseDTO todayCalories = getTodayCalories(user);

            String aiComment = dietPythonService.generateDailyComment(
                    todayCalories.getTotalCalories(),
                    healthInfo.getTargetCalories(),
                    healthInfo.getDietGoal()
            );

            return aiComment;

        } catch (Exception e) {
            return "오늘도 건강한 식단 관리를 이어가세요! 💪";
        }
    }

    @Transactional(readOnly = true)
    public MealDetailResponseDTO getMealDetail(Long userNo, String mealType) {
        // 1. 사용자 조회
        UserEntity user = userRepository.findById(userNo)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. 해당 끼니의 음식 목록 조회
        List<DietLogEntity> logs = getTodayMealByType(userNo, mealType);

        // 3. 총 영양소 계산
        NutritionDTO totalNutrition = calculateTotalNutritionDTO(logs);

        // 4. 목표 영양소 계산
        UserHealthInfoEntity healthInfo = healthInfoRepository.findByUser(user).orElse(null);
        NutritionDTO targetNutrition = nutritionTargetService.calculateMealTarget(healthInfo, mealType);

        // 5. 음식 목록 DTO 변환
        List<FoodItemDTO> foods = logs.stream()
                .map(this::convertToFoodItemDTO)
                .collect(Collectors.toList());

        return MealDetailResponseDTO.builder()
                .totalNutrition(totalNutrition)
                .targetNutrition(targetNutrition)
                .foods(foods)
                .build();
    }

    @Transactional(readOnly = true)
    public List<DietLogEntity> getTodayMealByType(Long userNo, String mealType) {
        LocalDate today = LocalDate.now();
        UserEntity user = userRepository.findById(userNo)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return dietLogRepository.findByUserAndEatDateAndMealType(user, today, mealType);
    }

    private NutritionDTO calculateTotalNutritionDTO(List<DietLogEntity> logs) {
        double totalCalories = 0;
        double totalCarbs = 0;
        double totalProtein = 0;
        double totalFat = 0;
        double totalFiber = 0;
        double totalSugar = 0;
        double totalWater = 0;
        double totalSodium = 0;
        double totalCalcium = 0;
        double totalMagnesium = 0;
        double totalIron = 0;
        double totalPotassium = 0;
        double totalVitaminA = 0;
        double totalVitaminC = 0;
        double totalVitaminD = 0;

        for (DietLogEntity log : logs) {
            FoodInfoEntity food = log.getFood();
            double quantity = log.getQuantity();

            totalCalories += food.getCalories() * quantity;
            totalCarbs += food.getCarbs() * quantity;
            totalProtein += food.getProtein() * quantity;
            totalFat += food.getFat() * quantity;

            FoodNutrientsEntity nutrients = foodNutrientsRepository.findById(food.getFoodNo()).orElse(null);

            if (nutrients != null) {
                if (nutrients.getFiber() != null) totalFiber += nutrients.getFiber() * quantity / 2.0;
                if (nutrients.getSugar() != null) totalSugar += nutrients.getSugar() * quantity;
                if (nutrients.getWater() != null) totalWater += nutrients.getWater() * quantity;
                if (nutrients.getSodium() != null) totalSodium += nutrients.getSodium() * quantity;
                if (nutrients.getCalcium() != null) totalCalcium += nutrients.getCalcium() * quantity;
                if (nutrients.getMagnesium() != null) totalMagnesium += nutrients.getMagnesium() * quantity / 10.0;
                if (nutrients.getIron() != null) totalIron += nutrients.getIron() * quantity / 10.0;
                if (nutrients.getPotassium() != null) totalPotassium += nutrients.getPotassium() * quantity / 10.0;
                if (nutrients.getVitaminA() != null) {
                    double vitaminA_IU = nutrients.getVitaminA();
                    double vitaminA_RAE = vitaminA_IU / 12.0;
                    totalVitaminA += vitaminA_RAE * quantity;
                }
                if (nutrients.getVitaminC() != null) {
                    totalVitaminC += nutrients.getVitaminC() * quantity / 10.0;
                }
                if (nutrients.getVitaminD() != null) {
                    double vitaminD_IU = nutrients.getVitaminD();
                    double vitaminD_mcg = vitaminD_IU / 40.0;
                    totalVitaminD += vitaminD_mcg * quantity;
                }
            }
        }

        return NutritionDTO.builder()
                .calories(round(totalCalories))
                .carbs(round(totalCarbs))
                .protein(round(totalProtein))
                .fat(round(totalFat))
                .fiber(round(totalFiber))
                .sugar(round(totalSugar))
                .water(round(totalWater))
                .sodium(round(totalSodium))
                .calcium(round(totalCalcium))
                .magnesium(round(totalMagnesium))
                .iron(round(totalIron))
                .potassium(round(totalPotassium))
                .vitaminA(round(totalVitaminA))
                .vitaminC(round(totalVitaminC))
                .vitaminD(round(totalVitaminD))
                .build();
    }

    private FoodItemDTO convertToFoodItemDTO(DietLogEntity log) {
        FoodInfoEntity food = log.getFood();
        double quantity = log.getQuantity();

        FoodNutrientsEntity nutrients = foodNutrientsRepository.findById(food.getFoodNo()).orElse(null);
        double fiber = 0.0;
        if (nutrients != null && nutrients.getFiber() != null) {
            fiber = round(nutrients.getFiber() * quantity);
        }

        return FoodItemDTO.builder()
                .logNo(log.getLogNo())
                .foodName(food.getFoodName())
                .calories(round(food.getCalories() * quantity))
                .carbs(round(food.getCarbs() * quantity))
                .protein(round(food.getProtein() * quantity))
                .fat(round(food.getFat() * quantity))
                .fiber(fiber)
                .build();
    }

    private double round(double value) {
        return Math.round(value * 10) / 10.0;
    }

    @Transactional(readOnly = true)
    public List<FoodInfoEntity> searchByName(String query, int limit) {
        return foodInfoRepository.findByFoodNameContainingOrderByCaloriesDesc(query, PageRequest.of(0, limit));
    }

    public List<DietLogResponseDTO> getTodayAllDiets() {
        LocalDate today = LocalDate.now();

        List<DietLogEntity> diets = dietLogRepository.findAllByEatDate(today);

        return diets.stream()
                .map(DietLogResponseDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteTodayAllDiets() {
        LocalDate today = LocalDate.now();

        dietLogRepository.deleteAllByEatDate(today);
    }


}
