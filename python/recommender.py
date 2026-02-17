# backend-python/recommender.py
from database import DBManager
from datetime import datetime, date


class FoodRecommender:
    def __init__(self):
        self.db = DBManager()
        # 슬롯별 카테고리 정의
        self.SLOTS = {
            'BASE': ['밥류', '면 및 만두류', '빵류', '죽 및 스프류'],
            'MAIN': ['육류', '수산물류', '알류', '찌개 및 전골류', '볶음류'],
            'SIDE': ['나물/무침류', '조림류', '김치류', '채소류', '장아찌/절임류']
        }

    def _get_recency_penalties(self, user_no):
        """[신규] 최근 식단 기록을 분석하여 음식별 감점(Penalty) 계산"""
        if not user_no: return {}

        # 최근 4일간의 기록만 가져옴
        query = """
            SELECT food_no, eat_date 
            FROM diet_log 
            WHERE user_no = %s AND eat_date >= DATE_SUB(CURDATE(), INTERVAL 4 DAY)
        """
        logs = self.db.fetch_all(query, (user_no,))

        penalties = {}
        today = date.today()

        for log in logs:
            food_no = log['food_no']
            eat_date = log['eat_date']

            # 날짜 차이 계산
            days_diff = (today - eat_date).days

            # 차이에 따른 패널티 부여 (여러 번 먹었을 경우 가장 큰 감점 적용)
            if days_diff == 0:
                penalty = 1000
            elif days_diff == 1:
                penalty = 500
            elif days_diff == 2:
                penalty = 300
            elif days_diff == 3:
                penalty = 100
            else:
                penalty = 0

            # 이미 기록이 있다면 더 큰 감점으로 업데이트
            penalties[food_no] = max(penalties.get(food_no, 0), penalty)

        return penalties

    def _get_user_preference(self, user_no):
        """사용자의 과거 상호작용(경험) 데이터를 가져옴"""
        if not user_no: return {}
        query = "SELECT food_no, view_count, eat_count FROM user_food_interaction WHERE user_no = %s"
        interactions = self.db.fetch_all(query, (user_no,))
        # {food_no: score} 형태로 변환
        return {i['food_no']: (i['eat_count'] * 10 - i['view_count'] * 2) for i in interactions}

    def get_slot_candidates(self, slot_name, target_cal, user_pref, recency_penalties):
        """기존 가중치 로직에 리센시 패널티(감점) 추가"""
        categories = self.SLOTS[slot_name]
        cat_placeholders = ", ".join(["%s"] * len(categories))

        query = f"""
            SELECT * FROM food_info 
            WHERE main_category IN ({cat_placeholders}) 
            AND is_meal = 1 
            AND calories BETWEEN %s AND %s
            ORDER BY nutrient_score DESC
            LIMIT 50 
        """
        params = categories + [target_cal * 0.5, target_cal * 1.5]
        candidates = self.db.fetch_all(query, params)

        scored_candidates = []
        for c in candidates:
            # 1. 영양 점수 (70%)
            n_score = c['nutrient_score']
            # 2. 선호도 점수 (30%)
            p_score = user_pref.get(c['food_no'], 0)
            # 3. [신규] 리센시 패널티 (감점 적용)
            r_penalty = recency_penalties.get(c['food_no'], 0)

            # 최종 점수 계산 로직
            total_score = (n_score * 0.7) + (p_score * 0.3) - r_penalty

            c['total_score'] = total_score
            scored_candidates.append(c)

        return sorted(scored_candidates, key=lambda x: x['total_score'], reverse=True)[:10]

    def evaluate_harmony(self, base, main, side):
        """음식 간의 조화(나트륨 균형 등)를 점수화"""
        # 1. 나트륨 과잉 체크 (세 음식의 합이 한 끼 권장량 800mg을 넘으면 감점)
        total_sodium = (base.get('sodium', 0) or 0) + (main.get('sodium', 0) or 0) + (side.get('sodium', 0) or 0)
        sodium_penalty = max(0, (total_sodium - 800) / 100)

        # 2. 카테고리 궁합
        pairing_bonus = 0
        if '국밥' in base['food_name'] and side['main_category'] == '김치류':
            pairing_bonus = 50

        return pairing_bonus - sodium_penalty

    def recommend_set(self, target_cal, user_no=None):
        """전체 추천 프로세스 통합"""
        # 데이터 수집
        user_pref = self._get_user_preference(user_no)
        recency_penalties = self._get_recency_penalties(user_no)  # 패널티 로직 호출

        # 슬롯별 후보군 추출 (패널티 데이터 전달)
        base_candidates = self.get_slot_candidates('BASE', target_cal * 0.4, user_pref, recency_penalties)
        main_candidates = self.get_slot_candidates('MAIN', target_cal * 0.45, user_pref, recency_penalties)
        side_candidates = self.get_slot_candidates('SIDE', target_cal * 0.15, user_pref, recency_penalties)

        if not base_candidates or not main_candidates:
            return None

        # 하모니 로직은 동일하게 유지
        best_set = None
        max_harmony = -9999

        # 상위 후보들끼리 교차 검증 (조합의 조화 확인)
        for b in base_candidates[:3]:
            for m in main_candidates[:3]:
                for s in side_candidates[:3]:
                    harmony_score = self.evaluate_harmony(b, m, s)
                    if harmony_score > max_harmony:
                        max_harmony = harmony_score
                        best_set = [b, m, s]

        return best_set

    def recommend_daily(self, user_no=None, guest_cal=None):
        """하루 3끼 전체 추천"""
        total_target = self._get_target_cal(user_no, guest_cal)

        # 끼니별 칼로리 배분
        splits = {"BREAKFAST": 0.3, "LUNCH": 0.4, "DINNER": 0.3}
        daily_plan = {}

        for meal, ratio in splits.items():
            meal_set = self.recommend_set(total_target * ratio, user_no)
            daily_plan[meal] = meal_set

        return daily_plan


    def _get_target_cal(self, user_no, guest_cal):
        if user_no:
            res = self.db.fetch_one("SELECT target_calories FROM user_health_info WHERE user_no = %s", (user_no,))
            return res['target_calories'] if res else 2000
        return guest_cal or 2000