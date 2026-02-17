package org.hsy.spring.repository;

import org.hsy.spring.entity.DietLogEntity;
import org.hsy.spring.entity.FoodInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DietLogRepository extends JpaRepository<DietLogEntity, Long> {

    List<DietLogEntity> findAllByUser_UserNoAndEatDate(Long userNo, LocalDate eatDate);

    @Query("SELECT d FROM DietLogEntity d JOIN FETCH d.food WHERE d.user.userNo = :userNo AND d.eatDate = :eatDate")
    List<DietLogEntity> findTodayDietWithFood(@Param("userNo") Long userNo, @Param("eatDate") LocalDate eatDate);

}
