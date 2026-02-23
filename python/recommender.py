from user_context import UserContext
from mealPlanner import MealPlanner


def _format_food(food: dict) -> dict:
    """API 응답용 음식 데이터 포맷"""
    return {
        'food_no':       food.get('food_no'),
        'food_name':     food.get('food_name'),
        'calories':      round(food.get('calories') or 0, 1),
        'protein':       round(food.get('protein') or 0, 1),
        'carbs':         round(food.get('carbs') or 0, 1),
        'fat':           round(food.get('fat') or 0, 1),
        'sodium':        round(food.get('sodium') or 0, 1),
        'fiber':         round(food.get('fiber') or 0, 1),
        'main_category': food.get('main_category'),
        'sub_category':  food.get('sub_category'),
    }


def _format_meal(meal: list) -> list:
    return [_format_food(f) for f in meal]


class GuestContext:
    """비로그인 사용자용 컨텍스트 - targetCal만 받아서 기본 영양소 계산"""
    def __init__(self, guest_cal: int):
        cal = guest_cal or 2000
        self.required_nutrients = {
            'calories': cal,
            'protein':  round((cal * 0.25) / 4, 1),
            'carbs':    round((cal * 0.50) / 4, 1),
            'fat':      round((cal * 0.25) / 9, 1),
        }
        self.interaction_scores = {}
        self.recent_food_nos = {}

    def get_preference_score(self, food_no: int) -> float:
        return 0.0

    def get_penalty_score(self, food_no: int) -> float:
        return 0.0


class Recommender:
    """
    API 진입점
    - recommend_daily  : /api/diet/recommend/daily
    - recommend_single : /api/diet/recommend/single

    회원:   user_no 있음, targetCal 없음
    비회원: user_no 없음, targetCal 있음 (?targetCal=2468)
    """

    def _get_context(self, user_no: int = None, guest_cal: int = None):
        """회원/비회원 컨텍스트 분기"""
        if user_no:
            try:
                return UserContext(user_no)
            except Exception as e:
                print(f"UserContext 로드 실패 (user_no={user_no}): {e} → 게스트로 전환")
                return GuestContext(guest_cal or 2000)
        return GuestContext(guest_cal or 2000)

    def recommend_daily(self, user_no=None, guest_cal=None):
        ctx = self._get_context(user_no, guest_cal)
        planner = MealPlanner(ctx)

        global_exclude = set(ctx.recent_food_nos.keys())
        daily_plan = {"BREAKFAST": [], "LUNCH": [], "DINNER": []}

        for meal_type in ["BREAKFAST", "LUNCH", "DINNER"]:
            meal = planner.plan_meal(meal_type, global_exclude)
            daily_plan[meal_type] = _format_meal(meal)
            global_exclude.update(f['food_no'] for f in meal if f.get('food_no'))

            if user_no and hasattr(ctx, 'update_interaction'):
                for food in meal:
                    if food.get('food_no'):
                        ctx.update_interaction(food['food_no'])

        return daily_plan

    def recommend_single(self, meal_type: str, current_plan: dict,
                         user_no: int = None, guest_cal: int = None) -> dict:
        """
        한 끼 단일 추천 (새로고침)
        current_plan: 이미 확정된 다른 끼니 {"BREAKFAST": [...], "LUNCH": [...]}
        반환: {"BREAKFAST": [...]} or {"LUNCH": [...]} or {"DINNER": [...]}
        """
        ctx = self._get_context(user_no, guest_cal)

        # 확정된 끼니 음식은 오늘 먹은 것으로 처리 → 중복 방지
        global_exclude = set(ctx.recent_food_nos.keys())
        for foods in current_plan.values():
            for food in foods:
                food_no = food.get('food_no')
                if food_no:
                    ctx.recent_food_nos[food_no] = 0
                    global_exclude.add(food_no)

        planner = MealPlanner(ctx)
        meal = planner.plan_meal(meal_type.upper(), global_exclude)

        if user_no and hasattr(ctx, 'update_interaction'):
            for food in meal:
                if food.get('food_no'):
                    ctx.update_interaction(food['food_no'])

        return _format_meal(meal)