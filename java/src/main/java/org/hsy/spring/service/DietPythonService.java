package org.hsy.spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class DietPythonService {

    private final String PYTHON_SERVER_URL = "http://localhost:8000/ai/recommend";
    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> getDailyPlan(Long userNo, Integer guestCal) {
        String url = String.format("%s/daily?%s", PYTHON_SERVER_URL,
                userNo != null ? "user_no=" + userNo : "guest_cal=" + guestCal);
        return restTemplate.getForObject(url, Map.class);
    }

    public Map<String, Object> getSingleMeal(String mealType, Long userNo, Integer guestCal) {
        String url = String.format("%s/single?meal_type=%s&%s", PYTHON_SERVER_URL, mealType,
                userNo != null ? "user_no=" + userNo : "guest_cal=" + guestCal);
        return restTemplate.getForObject(url, Map.class);
    }

    public String generateDailyComment(int currentCalories, int targetCalories, String dietGoal) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "currentCalories", currentCalories,
                    "targetCalories", targetCalories,
                    "dietGoal", dietGoal
            );

            Map<String, String> response = restTemplate.postForObject(
                    PYTHON_SERVER_URL + "/comment",
                    requestBody,
                    Map.class
            );

            return response != null ? response.get("comment") : getFallbackComment(currentCalories, targetCalories);

        } catch (Exception e) {
            return getFallbackComment(currentCalories, targetCalories);
        }
    }

    private String getFallbackComment(int currentCalories, int targetCalories) {
        double percentage = (double) currentCalories / targetCalories * 100;
        int remaining = targetCalories - currentCalories;

        if (percentage <= 110) {
            return String.format("현재까지 %,dkcal를 섭취하셨습니다. 목표의 %.0f%%를 달성하셨어요! 💪",
                    currentCalories, percentage);
        } else {
            return String.format("오늘 목표보다 %,dkcal 초과했어요. 내일 다시 시작해봐요! 💚",
                    Math.abs(remaining));
        }
    }

}