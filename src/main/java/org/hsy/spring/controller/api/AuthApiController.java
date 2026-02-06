package org.hsy.spring.controller.api;

import lombok.RequiredArgsConstructor;
import org.hsy.spring.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final UserRepository userRepository;

    @GetMapping("/check-id")
    public ResponseEntity<Boolean> checkId(@RequestParam String userId) {
        boolean exists = userRepository.existsByUserId(userId);
        return ResponseEntity.ok(exists);
    }


}