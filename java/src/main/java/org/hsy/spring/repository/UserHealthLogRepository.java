package org.hsy.spring.repository;

import org.hsy.spring.entity.UserHealthInfoEntity;
import org.hsy.spring.entity.UserHealthLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserHealthLogRepository extends JpaRepository<UserHealthLogEntity, Long>  {
}
