# backend-python/engines/daily_balancer.py
from datetime import date

class DailyBalancer:
    def __init__(self, db_manager):
        self.db = db_manager

    def get_user_info(self, user_no):
        """DB에서 유저의 최신 건강 정보를 조회"""
        if not user_no: return None
        query = "SELECT target_calories, diet_goal FROM user_health_info WHERE user_no = %s"
        return self.db.fetch_one(query, (user_no,))

    def get_user_context(self, user_no):
        """
        사용자의 선호도(7:3 로직)와 리센시 패널티(4일 감점 로직) 데이터 생성
        """
        if not user_no:
            return {}, {}

        # 1. 누적 선호도 점수
        pref_query = "SELECT food_no, view_count, eat_count FROM user_food_interaction WHERE user_no = %s"
        interactions = self.db.fetch_all(pref_query, (user_no,))
        user_pref = {
            i['food_no']: (i['eat_count'] * 10 - i['view_count'] * 2)
            for i in interactions
        }

        # 2. 패널티
        log_query = """
            SELECT food_no, eat_date 
            FROM diet_log 
            WHERE user_no = %s AND eat_date >= DATE_SUB(CURDATE(), INTERVAL 4 DAY)
        """
        logs = self.db.fetch_all(log_query, (user_no,))

        recency_penalties = {}
        today = date.today()

        for log in logs:
            f_no = log['food_no']
            e_date = log['eat_date']
            days_diff = (today - e_date).days

            penalty = {0: 1000, 1: 500, 2: 300, 3: 100}.get(days_diff, 0)

            recency_penalties[f_no] = max(recency_penalties.get(f_no, 0), penalty)

        return user_pref, recency_penalties

    def calculate_remaining_budget(self, daily_target, current_plan, target_meal_type):
        """
        [사용자님 아이디어] 기존 식단(아침/저녁 등)을 참고하여
        선택된 끼니(target_meal_type)의 남은 영양 예산과 중복 방지 리스트 반환
        """
        planned_cal = 0
        planned_protein = 0
        exclude_ids = []

        for m_type, meal_set in current_plan.items():
            if m_type != target_meal_type and meal_set:
                for food in meal_set:
                    planned_cal += food.get('calories', 0)
                    planned_protein += food.get('protein', 0)
                    exclude_ids.append(food['food_no'])

        base_ratios = {"BREAKFAST": 0.3, "LUNCH": 0.4, "DINNER": 0.3}
        standard_cal = daily_target.calories * base_ratios.get(target_meal_type, 0.3)

        remaining_cal_total = daily_target.calories - planned_cal

        actual_target_cal = max(300, remaining_cal_total)

        if len(current_plan.get(target_meal_type, [])) == 0:
            actual_target_cal = standard_cal

        return {
            'target_cal': actual_target_cal,
            'protein_needed': max(0, daily_target.protein_min - planned_protein),
            'exclude_ids': exclude_ids
        }