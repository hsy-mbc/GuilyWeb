from database import DBManager

db = DBManager()

# 끼니별 칼로리 배분 비율
MEAL_CAL_RATIO = {
    'BREAKFAST': 0.25,
    'LUNCH':     0.4,
    'DINNER':    0.35,
}

# 역할별 칼로리 배분 비율 (한 끼 예산 기준)
ROLE_CAL_RATIO = {
    'MAIN':      (0.35, 0.45),
    'MAIN_CARB': (0.35, 0.50),
    'MAIN_SOUP': (0.30, 0.45),
    'SUB_CARB':  (0.25, 0.35),
    'SUB_SOUP':  (0.10, 0.20),
    'SIDE':      (0.10, 0.20),
    'KIMCHI':    (0.03, 0.10),
}



class FoodSelector:

    def __init__(self, user_context):
        self.ctx = user_context

    def _score(self, food: dict, role: str, cal_target: float) -> float:
        food_no = food['food_no']

        penalty = self.ctx.get_penalty_score(food_no)
        if penalty <= -100:
            return -9999

        preference = self.ctx.get_preference_score(food_no)

        cal       = food.get('calories') or 0
        pro_den   = food.get('protein_density') or 0
        nut_score = food.get('nutrient_score') or 0
        sodium    = food.get('sodium') or 0

        cal_proximity = max(0, 100 - abs(cal - cal_target) / max(cal_target, 1) * 100)
        sodium_score  = max(0, 100 - (sodium / 2000) * 100)

        if role == 'MAIN':
            score = cal_proximity * 0.40 + pro_den * 1000 * 0.40 + preference * 0.20
        elif role == 'MAIN_CARB':
            score = cal_proximity * 0.50 + nut_score * 0.30 + preference * 0.20
        elif role == 'MAIN_SOUP':
            score = cal_proximity * 0.40 + pro_den * 1000 * 0.30 + nut_score * 0.10 + preference * 0.20
        elif role == 'SUB_CARB':
            score = cal_proximity * 0.30 + nut_score * 0.50 + preference * 0.20
        elif role == 'SUB_SOUP':
            score = sodium_score * 0.40 + nut_score * 0.40 + preference * 0.20
        elif role == 'SIDE':
            score = nut_score * 0.60 + (100 - cal_proximity) * 0.20 + preference * 0.20
        elif role == 'KIMCHI':
            score = sodium_score * 0.60 + nut_score * 0.20 + preference * 0.20
        else:
            score = nut_score * 0.60 + preference * 0.40

        return score + penalty

    def _fetch_candidates(self, role: str, cal_min: float, cal_max: float,
                          sodium_max: float = 2000) -> list:
        rows = db.fetch_all(
            """
            SELECT f.food_no, f.food_name, f.calories, f.protein, f.carbs, f.fat,
                   f.nutrient_score, f.protein_density,
                   f.main_category, f.sub_category,
                   fn.sodium, fn.fiber, fn.calcium, fn.iron,
                   fn.magnesium, fn.potassium, fn.vitamina, fn.vitaminc
            FROM food_info f
            JOIN food_role rm ON f.food_no = rm.food_no
            LEFT JOIN food_nutrients fn ON f.food_no = fn.food_no
            WHERE rm.role = %s
              AND f.calories BETWEEN %s AND %s
              AND (fn.sodium IS NULL OR fn.sodium <= %s)
              AND f.is_meal = 1
            ORDER BY f.nutrient_score DESC
            LIMIT 100
            """,
            (role, cal_min, cal_max, sodium_max)
        )
        return rows or []

    def _best(self, candidates: list, role: str, cal_target: float,
              exclude: set) -> dict | None:
        filtered = [f for f in candidates if f['food_no'] not in exclude]
        if not filtered:
            return None
        scored = sorted(filtered, key=lambda f: self._score(f, role, cal_target), reverse=True)
        return scored[0]

    def select_role(self, role: str, meal_cal: float, exclude: set,
                    sodium_max: float = 2000) -> dict | None:
        lo, hi = ROLE_CAL_RATIO.get(role, (0.1, 0.3))
        cal_min    = meal_cal * lo
        cal_max    = meal_cal * hi
        cal_target = meal_cal * ((lo + hi) / 2)
        candidates = self._fetch_candidates(role, cal_min, cal_max, sodium_max)
        return self._best(candidates, role, cal_target, exclude)

    def build_meal(self, meal_type: str, meal_cal: float, exclude: set) -> list:
        meal = []
        exclude = set(exclude)
        sodium_budget = 700.0

        def pick(role, sodium_max=None):
            nonlocal sodium_budget
            food = self.select_role(role, meal_cal, exclude, sodium_max or sodium_budget)
            if food:
                meal.append(food)
                exclude.add(food['food_no'])
                sodium_budget -= food.get('sodium') or 0
            return food

        # 아침 - 간단한 한식
        if meal_type == 'BREAKFAST':
            pick('SUB_SOUP')   # 가벼운 국물
            pick('SUB_CARB')   # 소량의 밥
            pick('SIDE')       # 반찬 1개
            if sodium_budget > 150:
                pick('KIMCHI') # 김치
            return meal

        # 점심/저녁
        main_role = None
        for role in ['MAIN', 'MAIN_CARB', 'MAIN_SOUP']:
            food = self.select_role(role, meal_cal, exclude)
            if food:
                main_role = role
                meal.append(food)
                exclude.add(food['food_no'])
                sodium_budget -= food.get('sodium') or 0
                break

        if not main_role:
            return meal

        if main_role == 'MAIN':
            pick('SUB_CARB')
            if sodium_budget > 200:
                pick('SUB_SOUP')
            pick('SIDE')
            if sodium_budget > 150:
                pick('KIMCHI')

        elif main_role == 'MAIN_CARB':
            pick('MAIN')
            pick('SIDE')
            if sodium_budget > 150:
                pick('KIMCHI')

        elif main_role == 'MAIN_SOUP':
            pick('SUB_CARB')
            pick('SIDE')
            if sodium_budget > 150:
                pick('KIMCHI')

        return meal






