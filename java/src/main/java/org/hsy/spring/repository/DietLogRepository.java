package org.hsy.spring.repository;

import org.hsy.spring.entity.DietLogEntity;
import org.hsy.spring.entity.FoodInfoEntity;
import org.hsy.spring.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface DietLogRepository extends JpaRepository<DietLogEntity, Long> {

    @Query("SELECT d FROM DietLogEntity d JOIN FETCH d.food WHERE d.user.userNo = :userNo AND d.eatDate = :eatDate")
    List<DietLogEntity> findTodayDietWithFood(@Param("userNo") Long userNo, @Param("eatDate") LocalDate eatDate);

    List<DietLogEntity> findByUserAndEatDate(UserEntity user, LocalDate eatDate);

    List<DietLogEntity> findByUserAndEatDateAndMealType(UserEntity user, LocalDate eatDate, String mealType);

    List<DietLogEntity> findAllByEatDate(LocalDate eatDate);

    @Modifying
    @Transactional
    void deleteAllByEatDate(LocalDate eatDate);


}
