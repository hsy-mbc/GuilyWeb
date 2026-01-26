package org.hsy.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hsy.spring.common.enums.UserRole;
import org.hsy.spring.common.enums.UserStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long userNo;
    private String userId;
    private String password;
    private String name;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime last_login;
}
