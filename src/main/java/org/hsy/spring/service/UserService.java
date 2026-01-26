package org.hsy.spring.service;

import lombok.RequiredArgsConstructor;
import org.hsy.spring.common.enums.UserRole;
import org.hsy.spring.common.enums.UserStatus;
import org.hsy.spring.entity.UserEntity;
import org.hsy.spring.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void join(UserEntity user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());

        UserEntity newUser = UserEntity.builder()
                .userId(user.getUserId())
                .password(encodedPassword)
                .name(user.getName())
                .role(UserRole.STAFF)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(newUser);
    }

}
