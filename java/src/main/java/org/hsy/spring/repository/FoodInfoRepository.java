package org.hsy.spring.repository;

import org.hsy.spring.entity.FoodInfoEntity;
import org.hsy.spring.entity.UserHealthInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodInfoRepository extends JpaRepository<FoodInfoEntity, Long> {

}
