package org.hsy.spring.controller;

import lombok.RequiredArgsConstructor;
import org.hsy.spring.entity.UserEntity;
import org.hsy.spring.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;

    @GetMapping("/")
    public String home(Model model) {
        return "index"; // main
    }

}