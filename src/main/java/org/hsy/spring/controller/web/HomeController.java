package org.hsy.spring.controller.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hsy.spring.dto.UserSignupDTO;
import org.hsy.spring.service.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;

    @GetMapping("/")
    public String home(Model model) {
        return "index";
    }

    @GetMapping("/index")
    public String index(Model model) {
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
                         BindingResult result,
                         RedirectAttributes rttr) {

        if (result.hasErrors()) {
            return "signup";
        }

        try {
            userService.join(userDTO);
            rttr.addFlashAttribute("message", "회원가입이 완료되었습니다! 로그인해주세요.");
            return "redirect:/login";

        } catch (DataIntegrityViolationException e) {
            rttr.addFlashAttribute("errorMessage", "이미 존재하는 아이디입니다.");
            return "redirect:/signup";
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "가입 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "redirect:/signup";
        }

    }

    @GetMapping("/diet")
    public String diet(Model model) {
        return "diet";
    }

}