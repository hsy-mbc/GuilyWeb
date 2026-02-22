package org.hsy.spring.service;

import lombok.RequiredArgsConstructor;
import org.hsy.spring.dto.NutritionDTO;
import org.hsy.spring.entity.UserHealthInfoEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NutritionTargetService {

    public NutritionDTO calculateMealTarget(UserHealthInfoEntity healthInfo, String mealType) {
        if (healthInfo == null) {
            return getDefaultTarget(mealType);
        }

        int dailyCalories = healthInfo.getTargetCalories();

        // 끼니별 칼로리 비율 (아침 30% : 점심 40% : 저녁 30%)
        double mealCalories = switch (mealType.toUpperCase()) {
            case "BREAKFAST" -> dailyCalories * 0.30;
            case "LUNCH" -> dailyCalories * 0.40;
            case "DINNER" -> dailyCalories * 0.30;
            default -> dailyCalories / 3.0;
        };

        double carbs = (mealCalories * 0.5) / 4;

        // 단백질: 체중 기반 계산 (활동량 고려)
        double dailyProtein = calculateProteinTarget(
                healthInfo.getWeight(),
                healthInfo.getActivityCoefficient()
        );
        double mealProtein = switch (mealType.toUpperCase()) {
            case "BREAKFAST" -> dailyProtein * 0.30;
            case "LUNCH" -> dailyProtein * 0.40;
            case "DINNER" -> dailyProtein * 0.30;
            default -> dailyProtein / 3.0;
        };

        double fat = (mealCalories * 0.25) / 9;

        // 기타 영양소 (끼니별 비율 적용)
        double mealRatio = switch (mealType.toUpperCase()) {
            case "BREAKFAST" -> 0.30;
            case "LUNCH" -> 0.40;
            case "DINNER" -> 0.30;
            default -> 1.0 / 3.0;
        };

        // 기타 영양소 (성별, 나이 고려)
        return NutritionDTO.builder()
                .calories(Math.round(mealCalories * 10) / 10.0)
                .carbs(Math.round(carbs * 10) / 10.0)
                .protein(Math.round(mealProtein * 10) / 10.0)
                .fat(Math.round(fat * 10) / 10.0)
                .fiber(Math.round(calculateFiberTarget(healthInfo.getGender()) * mealRatio * 10) / 10.0)
                .sugar(Math.round(calculateSugarTarget() * mealRatio * 10) / 10.0)
                .water(Math.round(calculateWaterTarget(healthInfo.getWeight(), healthInfo.getGender()) * mealRatio * 10) / 10.0)
                .sodium(Math.round(calculateSodiumTarget(healthInfo.getGender()) * mealRatio * 10) / 10.0)
                .calcium(Math.round(calculateCalciumTarget(healthInfo.getGender(), healthInfo.getAge()) * mealRatio * 10) / 10.0)
                .magnesium(Math.round(calculateMagnesiumTarget(healthInfo.getGender()) * mealRatio * 10) / 10.0)
                .iron(Math.round(calculateIronTarget(healthInfo.getGender(), healthInfo.getAge()) * mealRatio * 10) / 10.0)
                .potassium(Math.round(calculatePotassiumTarget(healthInfo.getGender()) * mealRatio * 10) / 10.0)
                .vitaminA(Math.round(calculateVitaminATarget(healthInfo.getGender()) * mealRatio * 10) / 10.0)
                .vitaminC(Math.round(calculateVitaminCTarget() * mealRatio * 10) / 10.0)
                .vitaminD(Math.round(calculateVitaminDTarget() * mealRatio * 10) / 10.0)
                .build();
    }

    /**
     * 단백질 목표 계산 (체중 기반)
     */
    private double calculateProteinTarget(Double weight, Double activityCoefficient) {
        if (weight == null) {
            return 60.0;
        }

        // 활동량에 따른 단백질 계수
        double proteinPerKg;

        if (activityCoefficient == null) {
            proteinPerKg = 1.2;
        } else if (activityCoefficient <= 1.2) {
            proteinPerKg = 1.0;
        } else if (activityCoefficient <= 1.375) {
            proteinPerKg = 1.2;
        } else if (activityCoefficient <= 1.55) {
            proteinPerKg = 1.5;
        } else if (activityCoefficient <= 1.725) {
            proteinPerKg = 1.8;
        } else {
            proteinPerKg = 2.0;
        }

        return weight * proteinPerKg;
    }

    /**
     * 식이섬유 목표 (성별 고려)
     */
    private double calculateFiberTarget(String gender) {
        return "MALE".equals(gender) ? 25.0 : 20.0;
    }

    /**
     * 당류 목표 (WHO 권장)
     */
    private double calculateSugarTarget() {
        return 50.0;
    }

    /**
     * 수분 목표 (체중, 성별 고려)
     */
    private double calculateWaterTarget(Double weight, String gender) {
        if (weight == null) {
            return "MALE".equals(gender) ? 2500.0 : 2000.0;
        }
        // 체중 1kg당 30~35ml
        return weight * 33.0;
    }

    /**
     * 나트륨 목표 (성별 고려)
     */
    private double calculateSodiumTarget(String gender) {
        return 2000.0; // WHO 권장 2000mg
    }

    /**
     * 칼슘 목표 (성별, 나이 고려)
     */
    private double calculateCalciumTarget(String gender, Integer age) {
        if (age != null && age >= 50) {
            return "FEMALE".equals(gender) ? 1200.0 : 1000.0; // 폐경기 여성 더 필요
        }
        return "MALE".equals(gender) ? 1000.0 : 800.0;
    }

    /**
     * 철분 목표 (성별, 나이 고려)
     */
    private double calculateIronTarget(String gender, Integer age) {
        if ("FEMALE".equals(gender) && age != null && age >= 19 && age <= 50) {
            return 18.0; // 가임기 여성
        }
        return "MALE".equals(gender) ? 10.0 : 8.0;
    }

    /**
     * 마그네슘 목표 (성별 고려)
     */
    private double calculateMagnesiumTarget(String gender) {
        return "MALE".equals(gender) ? 350.0 : 280.0;
    }

    /**
     * 칼륨 목표 (성별 고려)
     */
    private double calculatePotassiumTarget(String gender) {
        return "MALE".equals(gender) ? 3500.0 : 2800.0;
    }

    /**
     * 비타민 A 목표 (성별 고려)
     */
    private double calculateVitaminATarget(String gender) {
        return "MALE".equals(gender) ? 900.0 : 700.0; // μg RAE
    }

    /**
     * 비타민 C 목표
     */
    private double calculateVitaminCTarget() {
        return 100.0; // mg
    }

    /**
     * 비타민 D 목표
     */
    private double calculateVitaminDTarget() {
        return 10.0; // μg
    }

    /**
     * 소수점 반올림 (소수점 1자리)
     */
    private double round(double value) {
        return Math.round(value * 10) / 10.0;
    }

    /**
     * 기본 목표 (정보 없을 때)
     */
    private NutritionDTO getDefaultTarget(String mealType) {
        double ratio = switch (mealType.toUpperCase()) {
            case "BREAKFAST" -> 0.30;
            case "LUNCH" -> 0.40;
            case "DINNER" -> 0.30;
            default -> 1.0 / 3.0;
        };

        return NutritionDTO.builder()
                .calories(round(2100.0 * ratio))
                .carbs(round(262.5 * ratio))
                .protein(round(100.0 * ratio))
                .fat(round(58.3 * ratio))
                .fiber(round(25.0 * ratio))
                .sugar(round(50.0 * ratio))
                .water(round(2000.0 * ratio))
                .sodium(round(2000.0 * ratio))
                .calcium(round(1000.0 * ratio))
                .magnesium(round(350.0 * ratio))
                .iron(round(14.0 * ratio))
                .potassium(round(3500.0 * ratio))
                .vitaminA(round(800.0 * ratio))
                .vitaminC(round(100.0 * ratio))
                .vitaminD(round(10.0 * ratio))
                .build();
    }

}
