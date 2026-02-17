# backend-python/engines/target_engine.py

class DailyTarget:
    """하루 영양 가이드라인 정보를 담는 객체"""
    def __init__(self, calories, protein_min, sodium_max, fiber_min):
        self.calories = calories
        self.protein_min = protein_min
        self.sodium_max = sodium_max
        self.fiber_min = fiber_min

class TargetEngine:
    def __init__(self):
        # 기본 권장량 (성인 평균 기준 가이드)
        self.RDA = {
            'protein_ratio': 0.3,
            'sodium_cap': 2300,
            'fiber_goal': 25
        }

    def generate_daily_target(self, user_info, guest_cal):
        """
        user_info: {'target_calories': 2100, 'diet_goal': 'LOSS', ...}
        """
        if user_info:
            total_cal = user_info.get('target_calories', 2000)
            goal = user_info.get('diet_goal', 'MAINTAIN')
        else:
            total_cal = guest_cal or 2000
            goal = 'MAINTAIN'

        # 1. 단백질 목표 설정
        # 다이어트 중이라면 근손실 방지를 위해 단백질 비중을 높임
        p_ratio = 0.35 if goal == 'LOSS' else 0.25
        protein_min = (total_cal * p_ratio) / 4

        # 2. 나트륨 상한선
        sodium_max = 2000 if goal == 'LOSS' else 2300

        # 3. 식이섬유 최소치
        fiber_min = self.RDA['fiber_goal']

        return DailyTarget(
            calories=total_cal,
            protein_min=round(protein_min, 1),
            sodium_max=sodium_max,
            fiber_min=fiber_min
        )