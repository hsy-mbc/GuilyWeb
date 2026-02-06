package org.hsy.spring.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hsy.spring.common.enums.UserRole;
import org.hsy.spring.common.enums.UserStatus;
import org.hsy.spring.dto.UserSignupDTO;
import org.hsy.spring.entity.UserEntity;
import org.hsy.spring.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void join(UserSignupDTO dto) {

        if (userRepository.existsByUserId(dto.getUserId())) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        // 비밀번호 암호화
        String encodedPw = passwordEncoder.encode(dto.getPassword());

        UserEntity userEntity = UserEntity.builder()
                .userId(dto.getUserId())
                .password(encodedPw)
                .name(dto.getName())
                .gender(dto.getGender())
                .birthdate(dto.getBirthdate())
                .height(dto.getHeight())
                .weight(dto.getWeight())
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(userEntity);
    }

}
