package org.hsy.spring.controller.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class StatisticsController {

    @GetMapping("/Statistics")
    public String Statistics(Model model) { return "Statistics"; }

}
