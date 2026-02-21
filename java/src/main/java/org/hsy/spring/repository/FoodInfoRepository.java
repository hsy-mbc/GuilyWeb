package org.hsy.spring.repository;

import org.hsy.spring.entity.FoodInfoEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodInfoRepository extends JpaRepository<FoodInfoEntity, Long> {

    List<FoodInfoEntity> findByFoodNameContainingOrderByCaloriesDesc(String foodName, Pageable pageable);

}
