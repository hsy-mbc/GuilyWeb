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

        double percentage = (double) currentCalories / targetCalories * 100;
        int remaining = targetCalories - currentCalories;

        if (percentage < 50) {
            return String.format("현재까지 %,dkcal를 섭취하셨네요! 목표 칼로리의 %.0f%%를 달성하셨습니다. " +
                            "영양소를 골고루 섭취하며 %,dkcal를 더 섭취하시면 목표를 달성할 수 있어요! 💪",
                    currentCalories, percentage, remaining);
        } else if (percentage < 90) {
            return String.format("현재까지 %,dkcal를 섭취하셨네요! 목표 칼로리의 %.0f%%를 달성하셨습니다. " +
                            "저녁 식사로 단백질이 풍부한 음식을 선택하시면 오늘의 목표를 완벽하게 달성할 수 있어요! 👍",
                    currentCalories, percentage);
        } else if (percentage <= 110) {
            return String.format("훌륭해요! 현재까지 %,dkcal를 섭취하셨고, 목표 칼로리를 %.0f%% 달성하셨습니다. " +
                            "꾸준히 실천하고 계시니 목표를 반드시 이루실 수 있을 거예요! 🎉",
                    currentCalories, percentage);
        } else {
            return String.format("현재까지 %,dkcal를 섭취하셨네요. 목표보다 %,dkcal 초과했습니다. " +
                            "내일은 조금 더 조절하며 건강한 식단을 유지해보세요! 괜찮아요, 다시 시작하면 됩니다! 💚",
                    currentCalories, Math.abs(remaining));
        }
    }

}