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

}