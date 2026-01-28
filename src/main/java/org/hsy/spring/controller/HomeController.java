package org.hsy.spring.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hsy.spring.dto.UserSignupDTO;
import org.hsy.spring.entity.UserEntity;
import org.hsy.spring.service.UserDetailService;
import org.hsy.spring.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;

    @GetMapping("/")
    public String home(Model model) {
        return "index";
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute("userDTO") UserSignupDTO userDTO,
                         BindingResult result) {
        System.out.println("회원가입 요청 들어옴!");

        // 1. 유효성 검사 실패 시
        if (result.hasErrors()) {
            System.out.println("에러 발생: " + result.getAllErrors());
            return "signup"; // 에러 메시지와 함께 가입 페이지로 귀환
        }

        // 2. 서비스에 DTO 전달 (서비스에서 엔티티로 변환 후 저장)
        userService.join(userDTO);

        return "redirect:/login";
    }
}