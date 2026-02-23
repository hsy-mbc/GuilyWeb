from database import DBManager
from user_context import UserContext
from foodSelector import FoodSelector

db = DBManager()


class MealPlanner:
    def __init__(self, user_context):
        self.ctx = user_context
        self.selector = FoodSelector(user_context)

    def plan_meal(self, meal_type: str, global_exclude: set = None) -> list:
        nutrients = self.ctx.required_nutrients
        total_cal = nutrients['calories']

        cal_budget_map = {
            'BREAKFAST': total_cal * 0.25,
            'LUNCH':     total_cal * 0.4,
            'DINNER':    total_cal * 0.35,
        }
        cal_budget = cal_budget_map.get(meal_type, total_cal * 0.33)
        exclude = set(global_exclude or set()) | set(self.ctx.recent_food_nos.keys())

        print(f"\n[{meal_type}] 칼로리 예산: {cal_budget}kcal")
        meal = self.selector.build_meal(meal_type, cal_budget, exclude)
        print(f"  구성: {[f['food_name'] for f in meal]}")
        total_cal = sum(f.get('calories') or 0 for f in meal)
        print(f"  총 칼로리: {total_cal:.0f}kcal")

        return meal

    def plan_daily(self) -> dict:
        return {
            'BREAKFAST': self.plan_meal('BREAKFAST'),
            'LUNCH':     self.plan_meal('LUNCH'),
            'DINNER':    self.plan_meal('DINNER'),
        }