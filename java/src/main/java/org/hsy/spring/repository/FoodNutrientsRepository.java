package org.hsy.spring.repository;

import org.hsy.spring.entity.FoodNutrientsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodNutrientsRepository extends JpaRepository<FoodNutrientsEntity, Long> {
}