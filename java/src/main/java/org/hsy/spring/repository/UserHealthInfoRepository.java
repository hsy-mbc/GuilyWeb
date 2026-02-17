package org.hsy.spring.repository;

import org.hsy.spring.entity.UserEntity;
import org.hsy.spring.entity.UserHealthInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserHealthInfoRepository extends JpaRepository<UserHealthInfoEntity, Long> {
    Optional<UserHealthInfoEntity> findByUser(UserEntity user);
    boolean existsByUser(UserEntity user);
}
