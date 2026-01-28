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
        // 비밀번호 암호화
        String encodedPw = passwordEncoder.encode(dto.getPassword());

        // DTO -> Entity 변환
        UserEntity userEntity = UserEntity.builder()
                .userId(dto.getUserId())
                .password(encodedPw)
                .name(dto.getName())
                .gender(dto.getGender())
                .birthdate(dto.getBirthdate())
                .height(dto.getHeight())
                .weight(dto.getWeight())
                .role(UserRole.CUSTOMER) // 기본값 설정
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(userEntity);
    }

}
