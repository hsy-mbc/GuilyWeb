# backend-python/engines/meal_builder.py
import random


class MealBuilder:
    def __init__(self, db_manager, culture_map):
        self.db = db_manager
        self.cm = culture_map  # CultureMap 객체를 주입받음

        # 슬롯별 DB 카테고리 매핑
        self.SLOTS = {
            'PROTEIN': ['육류', '수산물류', '알류', '볶음류', '찌개 및 전골류'],
            'CARB': ['밥류', '면 및 만두류', '빵류', '죽 및 스프류'],
            'FIBER': ['나물/무침류', '조림류', '김치류', '채소류', '샐러드']
        }

    def build(self, meal_type, budget, context):
        """
        한 끼 식단을 조립하는 메인 메서드
        budget: {target_cal, protein_needed, exclude_ids}
        context: {user_pref, recency_p}
        """
        if meal_type == "BREAKFAST":
            return self._build_breakfast(budget, context)
        return self._build_standard(budget, context)

    def _build_breakfast(self, budget, context):
        """아침 전용: 고단백 위주의 간단한 2슬롯 식단"""
        meal_set = []
        target_cal = budget['target_cal']

        # 1. 단백질 앵커 (유제품, 계란 등 가벼운 것 우선)
        anchor = self._get_food_from_db('PROTEIN', target_cal * 0.6, context, budget['exclude_ids'],
                                        pref_cats=['알류', '우유 및 유제품류', '육류'])
        if anchor: meal_set.append(anchor)

        # 2. 가벼운 탄수화물
        used_ids = budget['exclude_ids'] + [f['food_no'] for f in meal_set]
        side = self._get_food_from_db('CARB', target_cal * 0.4, context, used_ids,
                                      pref_cats=['빵류', '과일류', '죽 및 스프류'])
        if side: meal_set.append(side)

        return meal_set

    def _build_standard(self, budget, context):
        """점심/저녁: 30~70% 앵커 비중과 문화적 조화를 고려한 정찬"""
        meal_set = []
        target_cal = budget['target_cal']

        # 1. 메인 단백질원 (Anchor) 결정
        # 예산의 절반 정도를 기준으로 후보군 추출
        anchor = self._get_food_from_db('PROTEIN', target_cal * 0.5, context, budget['exclude_ids'])
        if not anchor: return []
        meal_set.append(anchor)

        # 앵커의 성격 파악 (문화권, 식감, 맛)
        culture = self.cm.detect_culture(anchor)
        is_wet = self.cm.is_wet(anchor)
        is_spicy = self.cm.is_spicy(anchor)

        # 2. 주식 (Carb) 결정 - 앵커와 문화권/식감 맞춤
        used_ids = budget['exclude_ids'] + [f['food_no'] for f in meal_set]
        base = self._get_food_from_db('CARB', target_cal * 0.35, context, used_ids,
                                      culture=culture, is_wet_anchor=is_wet)
        if base: meal_set.append(base)

        # 3. 반찬 (Fiber) 결정 - 문화권/맛 밸런스 맞춤
        used_ids = budget['exclude_ids'] + [f['food_no'] for f in meal_set]
        side = self._get_food_from_db('FIBER', target_cal * 0.15, context, used_ids,
                                      culture=culture, is_spicy_anchor=is_spicy)
        if side: meal_set.append(side)

        return meal_set

    def _get_food_from_db(self, slot, target_cal, context, exclude_ids,
                          culture=None, pref_cats=None, is_wet_anchor=False, is_spicy_anchor=False):
        """DB 쿼리 및 가중치 계산 핵심 로직"""
        cats = pref_cats if pref_cats else self.SLOTS[slot]

        # 1차 필터링: 카테고리와 칼로리 범위 (유연하게 +- 50% 허용)
        query = f"""
            SELECT * FROM food_info 
            WHERE main_category IN ({','.join(['%s'] * len(cats))}) 
            AND calories BETWEEN %s AND %s
        """
        params = list(cats) + [target_cal * 0.5, target_cal * 1.5]

        if exclude_ids:
            query += f" AND food_no NOT IN ({','.join(['%s'] * len(exclude_ids))})"
            params += list(exclude_ids)

        candidates = self.db.fetch_all(query, params)
        if not candidates: return None

        # --- 가중치 점수 계산 (Scoring Engine) ---
        for c in candidates:
            # (1) 기본 7:3 로직 + 리센시 감점
            # 영양가치(7) + 유저선호(3) - 과거기록패널티
            score = (c['nutrient_score'] * 0.7) + (context['user_pref'].get(c['food_no'], 0) * 0.3)
            score -= context['recency_p'].get(c['food_no'], 0)

            # (2) 문화적 조화 가중치 (CultureMap 활용)
            if culture:
                score += self.cm.get_harmony_bonus(culture, c)

            # (3) 식감 조화 패널티 (국물 + 국물 방지)
            if is_wet_anchor and self.cm.is_wet(c):
                score -= 150  # 강력 감점

            # (4) 맛의 조화 패널티 (빨간맛 + 빨간맛 방지)
            if is_spicy_anchor and self.cm.is_spicy(c):
                score -= 100

            c['final_calc_score'] = score

        # 최종 점수 순 정렬 후 상위 3개 중 랜덤 추출하여 다양성 확보
        candidates.sort(key=lambda x: x['final_calc_score'], reverse=True)
        return random.choice(candidates[:3])