package org.hsy.spring.controller.web;

import lombok.RequiredArgsConstructor;
import org.hsy.spring.entity.DietLogEntity;
import org.hsy.spring.entity.UserEntity;
import org.hsy.spring.security.CustomUserDetails;
import org.hsy.spring.service.DietService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DietController {

    private final DietService dietService;

    @GetMapping("/health_setup")
    public String healthSetup() {
        return "health_setup";
    }

    @GetMapping("/diet")
    public String diet() {
        return "diet";
    }

    @GetMapping("/diet_edit")
    public String dietEdit(@RequestParam(required = false) String meal, Model model) {
        if (meal == null || meal.isEmpty()) {
            meal = "breakfast";
        }

        meal = meal.toLowerCase();

        if (!meal.equals("breakfast") && !meal.equals("lunch") && !meal.equals("dinner")) {
            meal = "breakfast";
        }

        model.addAttribute("mealType", meal);

        return "diet_edit";
    }


    @GetMapping("/diet-set")
    public String dietSet(@AuthenticationPrincipal CustomUserDetails userDetails,
                           @RequestParam(required = false) Integer guestCal,
                           Model model) {
        UserEntity user = userDetails.getUser();

        if (user != null) {
            List<DietLogEntity> todayLogs = dietService.getTodayDietLogs(user.getUserNo());
            model.addAttribute("todayLogs", todayLogs);
            model.addAttribute("isMember", true);

        } else if (guestCal != null) {
            model.addAttribute("targetCal", guestCal);
            model.addAttribute("isMember", false);
        }

        return "diet";
    }

    @GetMapping("/diet-entry")
    public String dietEntry(@AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return "redirect:/health_setup";
        }

        boolean hasHealthInfo = dietService.hasHealthInfo(userDetails.getUser());

        if (hasHealthInfo) {
            return "redirect:/diet-set";
        } else {
            return "redirect:/health_setup";
        }
    }


}
