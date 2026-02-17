# backend-python/engines/recommender.py
from target_engine import TargetEngine
from culture_map import CultureMap
from daily_balancer import DailyBalancer
from meal_builder import MealBuilder


class FoodRecommender:
    def __init__(self, db_manager):
        self.db = db_manager
        self.target_engine = TargetEngine()
        self.culture_map = CultureMap()
        self.balancer = DailyBalancer(db_manager)
        self.builder = MealBuilder(db_manager, self.culture_map)

    def recommend_daily(self, user_no=None, guest_cal=None):
        """[하루 전체 추천] 아침 -> 점심 -> 저녁 순으로 조화로운 식단 구성"""

        # 1. 하루 영양 가이드라인 설정
        user_info = self.balancer.get_user_info(user_no)
        daily_target = self.target_engine.generate_daily_target(user_info, guest_cal)

        # 2. 사용자 경험 데이터 로드
        user_pref, recency_p = self.balancer.get_user_context(user_no)

        # 3. 끼니별 순차 생성 (Meal Builder)
        global_exclude = []
        daily_plan = {"BREAKFAST": [], "LUNCH": [], "DINNER": []}

        for meal_type in ["BREAKFAST", "LUNCH", "DINNER"]:
            budget = self.balancer.calculate_remaining_budget(daily_target, daily_plan, meal_type)
            context = {
                'user_pref': user_pref,
                'recency_p': recency_p
            }
            meal_set = self.builder.build(meal_type, budget, context)
            daily_plan[meal_type] = meal_set
            # 다음 끼니에서 중복되지 않도록 ID 누적
            if meal_set:
                global_exclude.extend([f['food_no'] for f in meal_set])

        return daily_plan

    def recommend_single(self, meal_type, current_plan, user_no=None, guest_cal=None):
        """[한 끼 새로고침] 기존 아침/저녁 정보를 참고하여 특정 끼니만 다시 추천"""

        # 1. 목표치 및 유저 데이터 준비
        user_info = self.balancer.get_user_info(user_no)
        daily_target = self.target_engine.generate_daily_target(user_info, guest_cal)
        user_pref, recency_p = self.balancer.get_user_context(user_no)

        # 2. 다른 끼니를 참고한 이번 끼니 전용 예산 및 금지 목록 계산
        budget = self.balancer.calculate_remaining_budget(daily_target, current_plan, meal_type)

        context = {
            'user_pref': user_pref,
            'recency_p': recency_p
        }

        return self.builder.build(meal_type, budget, context)