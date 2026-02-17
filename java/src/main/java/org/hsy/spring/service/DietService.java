package org.hsy.spring.service;

import org.hsy.spring.repository.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.hsy.spring.dto.DietLogRequestDTO;
import org.hsy.spring.dto.HealthCalculateRequestDTO;
import org.hsy.spring.entity.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DietService {

    private final UserHealthInfoRepository healthInfoRepository;
    private final UserHealthLogRepository healthLogRepository;
    private final DietLogRepository dietLogRepository;
    private final FoodInfoRepository foodInfoRepository;

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


    @Transactional
    public Long saveDiet(UserEntity user, DietLogRequestDTO request) {
        FoodInfoEntity food = foodInfoRepository.findById(request.getFoodNo())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 음식입니다."));

        DietLogEntity log = DietLogEntity.builder()
                .user(user)
                .food(food)
                .mealType(request.getMealType())
                .quantity(request.getQuantity())
                .eatDate(request.getEatDate())
                .build();

        return dietLogRepository.save(log).getLogNo();
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
    public void deleteDiet(Long logNo) {
        dietLogRepository.deleteById(logNo);
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

}
